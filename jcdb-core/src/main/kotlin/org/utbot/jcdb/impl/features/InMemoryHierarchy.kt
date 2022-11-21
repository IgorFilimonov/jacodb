package org.utbot.jcdb.impl.features

import org.jooq.DSLContext
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.utbot.jcdb.api.ByteCodeIndexer
import org.utbot.jcdb.api.ClassSource
import org.utbot.jcdb.api.JCDB
import org.utbot.jcdb.api.JCDBPersistence
import org.utbot.jcdb.api.JcClassOrInterface
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.JcFeature
import org.utbot.jcdb.api.JcSignal
import org.utbot.jcdb.api.RegisteredLocation
import org.utbot.jcdb.impl.bytecode.JcClassOrInterfaceImpl
import org.utbot.jcdb.impl.fs.ClassSourceImpl
import org.utbot.jcdb.impl.fs.className
import org.utbot.jcdb.impl.storage.BatchedSequence
import org.utbot.jcdb.impl.storage.jooq.tables.references.CLASSES
import org.utbot.jcdb.impl.storage.jooq.tables.references.CLASSHIERARCHIES
import org.utbot.jcdb.impl.storage.jooq.tables.references.SYMBOLS
import org.utbot.jcdb.impl.vfs.LazyPersistentByteCodeLocation
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

typealias InMemoryHierarchyCache = ConcurrentHashMap<Long, ConcurrentHashMap<Long, MutableSet<Long>>>

private val objectJvmName = Type.getInternalName(Any::class.java)

class FastHierarchyIndexer(
    private val persistence: JCDBPersistence,
    private val location: RegisteredLocation,
    private val hierarchy: InMemoryHierarchyCache
) : ByteCodeIndexer {

    override fun index(classNode: ClassNode) {
        val clazzSymbolId = persistence.findSymbolId(classNode.name.className) ?: return
        val superName = classNode.superName
        val superclasses = when {
            superName != null && superName != objectJvmName -> classNode.interfaces + superName
            else -> classNode.interfaces
        }
        superclasses.mapNotNull { persistence.findSymbolId(it.className) }
            .forEach {
                hierarchy.getOrPut(it) { ConcurrentHashMap() }
                    .getOrPut(location.id) { ConcurrentHashMap.newKeySet() }
                    .add(clazzSymbolId)
            }
    }

    override fun flush(jooq: DSLContext) {
    }
}

data class FastHierarchyReq(val name: String, val allHierarchy: Boolean = true)

object InMemoryHierarchy : JcFeature<FastHierarchyReq, ClassSource> {

    private val hierarchies = ConcurrentHashMap<JCDB, InMemoryHierarchyCache>()

    override fun onSignal(signal: JcSignal) {
        when (signal) {
            is JcSignal.BeforeIndexing -> {
                signal.jcdb.persistence.read { jooq ->
                    val cache = InMemoryHierarchyCache().also {
                        hierarchies[signal.jcdb] = it
                    }
                    jooq.select(CLASSES.NAME, CLASSHIERARCHIES.SUPER_ID, CLASSES.LOCATION_ID)
                        .from(CLASSHIERARCHIES)
                        .join(CLASSES).on(CLASSHIERARCHIES.CLASS_ID.eq(CLASSES.ID))
                        .fetch().forEach { (classSymbolId, superClassId, locationId) ->
                            cache.getOrPut(superClassId!!) { ConcurrentHashMap() }
                                .getOrPut(locationId!!) { ConcurrentHashMap.newKeySet() }
                                .add(classSymbolId!!)
                        }
                }
            }

            is JcSignal.LocationRemoved -> {
                signal.jcdb.persistence.write {
                    val id = signal.location.id
                    hierarchies[signal.jcdb]?.values?.forEach {
                        it.remove(id)
                    }
                }
            }

            is JcSignal.Drop -> {
                hierarchies[signal.jcdb]?.clear()
            }

            else -> Unit
        }
    }

    override suspend fun query(classpath: JcClasspath, req: FastHierarchyReq): Sequence<ClassSource> {
        return syncQuery(classpath, req)
    }

    fun syncQuery(classpath: JcClasspath, req: FastHierarchyReq): Sequence<ClassSource> {
        val persistence = classpath.db.persistence
        val locationIds = classpath.registeredLocations.map { it.id }
        if (req.name == "java.lang.Object") {
            return BatchedSequence(50) { offset, batchSize ->
                persistence.read { jooq ->
                    val whereCondition = if (offset == null) {
                        CLASSES.LOCATION_ID.`in`(locationIds)
                    } else {
                        CLASSES.LOCATION_ID.`in`(locationIds).and(
                            CLASSES.ID.greaterThan(offset)
                        )
                    }

                    jooq.select(CLASSES.ID, SYMBOLS.NAME, CLASSES.BYTECODE, CLASSES.LOCATION_ID)
                        .from(CLASSES)
                        .join(SYMBOLS).on(SYMBOLS.ID.eq(CLASSES.NAME))
                        .where(whereCondition)
                        .orderBy(CLASSES.ID)
                        .limit(batchSize)
                        .fetch()
                        .mapNotNull { (classId, className, byteCode, locationId) ->
                            classId!! to ClassSourceImpl(
                                LazyPersistentByteCodeLocation(persistence, locationId!!, classpath.db.runtimeVersion),
                                className!!, byteCode!!
                            )
                        }
                }
            }
        }
        val hierarchy = hierarchies[classpath.db] ?: return emptySequence()

        fun getSubclasses(
            symbolId: Long,
            locationIds: Set<Long>,
            transitive: Boolean,
            result: HashSet<Long>
        ) {
            val subclasses = hierarchy[symbolId]?.entries?.flatMap {
                when {
                    locationIds.contains(it.key) -> it.value
                    else -> emptyList()
                }
            }.orEmpty().toSet()
            result.addAll(subclasses)
            if (transitive) {
                subclasses.forEach {
                    getSubclasses(it, locationIds, true, result)
                }
            }

        }

        val classSymbol = persistence.findSymbolId(req.name) ?: return emptySequence()

        val allSubclasses = hashSetOf<Long>()
        getSubclasses(classSymbol, locationIds.toSet(), req.allHierarchy, allSubclasses)
        if (allSubclasses.isEmpty()) {
            return emptySequence()
        }
        val allHashes = allSubclasses.toList()
        return BatchedSequence<ClassSource>(50) { offset, batchSize ->
            persistence.read { jooq ->
                val index = offset?.toInt() ?: 0
                val hashes = allHashes.subList(index, min(allHashes.size, index + batchSize))
                if (hashes.isEmpty()) {
                    emptyList()
                } else {
                    jooq.select(CLASSES.ID, SYMBOLS.NAME, CLASSES.BYTECODE, CLASSES.LOCATION_ID)
                        .from(CLASSES)
                        .join(SYMBOLS).on(SYMBOLS.ID.eq(CLASSES.NAME))
                        .where(SYMBOLS.ID.`in`(hashes).and(CLASSES.LOCATION_ID.`in`(locationIds)))
                        .fetch()
                        .mapNotNull { (classId, className, byteCode, locationId) ->
                            (index.toLong() + batchSize) to ClassSourceImpl(
                                LazyPersistentByteCodeLocation(persistence, locationId!!, classpath.db.runtimeVersion),
                                className!!, byteCode!!
                            )
                        }
                }
            }
        }
    }

    override fun newIndexer(jcdb: JCDB, location: RegisteredLocation): ByteCodeIndexer {
        return FastHierarchyIndexer(jcdb.persistence, location, hierarchies.getOrPut(jcdb) { ConcurrentHashMap() })
    }

}

class InMemoryHierarchyExtension(private val cp: JcClasspath) {

    fun findSubClasses(name: String, allHierarchy: Boolean): Sequence<JcClassOrInterface> {
        return InMemoryHierarchy.syncQuery(cp, FastHierarchyReq(name, allHierarchy)).map {
            JcClassOrInterfaceImpl(cp, it)
        }
    }
}

suspend fun JcClasspath.inMemoryHierarchyExt(): InMemoryHierarchyExtension {
    db.awaitBackgroundJobs()
    return InMemoryHierarchyExtension(this)
}

internal fun JcClasspath.findSubclassesInMemory(name: String, allHierarchy: Boolean): Sequence<JcClassOrInterface> {
    return InMemoryHierarchy.syncQuery(this, FastHierarchyReq(name, allHierarchy)).map {
        JcClassOrInterfaceImpl(this, it)
    }
}