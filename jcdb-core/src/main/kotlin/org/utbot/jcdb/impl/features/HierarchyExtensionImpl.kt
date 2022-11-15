package org.utbot.jcdb.impl.features

import org.utbot.jcdb.api.JcClassOrInterface
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.JcMethod
import org.utbot.jcdb.api.ext.HierarchyExtension
import org.utbot.jcdb.api.findMethodOrNull
import org.utbot.jcdb.api.isPrivate
import org.utbot.jcdb.impl.bytecode.JcClassOrInterfaceImpl
import org.utbot.jcdb.impl.fs.ClassSourceImpl

@Suppress("SqlResolve")
class HierarchyExtensionImpl(private val cp: JcClasspath) : HierarchyExtension {

    companion object {
        private fun allHierarchyQuery(locationIds: String, sinceId: Long?) = """
            WITH RECURSIVE Hierarchy(class_name_id, class_id) AS (
                SELECT Classes.name, ClassHierarchies.class_id FROM ClassHierarchies
                    JOIN Symbols ON Symbols.id = ClassHierarchies.super_id
                    JOIN Classes ON Classes.id = ClassHierarchies.class_id
                    WHERE Symbols.name = ? and ($sinceId is null or ClassHierarchies.class_id > $sinceId)
                UNION ALL
                SELECT Classes.name, ClassHierarchies.class_id FROM ClassHierarchies
                    JOIN Classes ON Classes.id = ClassHierarchies.class_id
                    JOIN Hierarchy ON Hierarchy.class_name_id = ClassHierarchies.super_id
                    WHERE $sinceId is null or ClassHierarchies.id > $sinceId)
            SELECT DISTINCT Classes.id, Classes.location_id,  Symbols.name as name_name, Classes.bytecode from Hierarchy
                JOIN Classes ON Classes.id = hierarchy.class_id
                JOIN Symbols ON Symbols.id = Classes.name
             WHERE location_id in ($locationIds)
        """.trimIndent()

        private fun directSubClassesQuery(locationIds: String, sinceId: Long?) = """
            SELECT Classes.id, Classes.location_id, SymbolsName.name as name_name, Classes.bytecode FROM ClassHierarchies
                JOIN Symbols ON Symbols.id = ClassHierarchies.super_id
                JOIN Symbols as SymbolsName ON SymbolsName.id = Classes.name
                JOIN Classes ON Classes.id = ClassHierarchies.class_id
            WHERE Symbols.name = ? and ($sinceId is null or ClassHierarchies.class_id > $sinceId) AND Classes.location_id in ($locationIds) 
        """.trimIndent()

    }

    override fun findSubClasses(name: String, allHierarchy: Boolean): Sequence<JcClassOrInterface> {
        val classId = cp.findClassOrNull(name) ?: return emptySequence()
        return findSubClasses(classId, allHierarchy)
    }

    override fun findSubClasses(classId: JcClassOrInterface, allHierarchy: Boolean): Sequence<JcClassOrInterface> {
        val name = classId.name

        return cp.subClasses(name, allHierarchy).map { record ->
            JcClassOrInterfaceImpl(
                cp, ClassSourceImpl(
                    location = cp.registeredLocations.first { it.id == record.locationId },
                    className = record.name,
                    byteCode = record.byteCode
                )
            )
        }
    }

    override fun findOverrides(methodId: JcMethod): Sequence<JcMethod> {
        val desc = methodId.description
        val name = methodId.name
        val subClasses = findSubClasses(methodId.enclosingClass, allHierarchy = true)
        return subClasses.mapNotNull {
            it.findMethodOrNull(name, desc)
        }.filter { !it.isPrivate }
    }

    private fun JcClasspath.subClasses(name: String, allHierarchy: Boolean): Sequence<ClassRecord> {
        val locationIds = registeredLocations.joinToString(", ") { it.id.toString() }
        return BatchedSequence(50) {
            val query = if (allHierarchy) allHierarchyQuery(locationIds, it) else directSubClassesQuery(locationIds, it)
            db.persistence.read {
                val cursor = it.fetchLazy(query, name)
                cursor.fetchNext(50).map {
                    ClassRecord(
                        byteCode = it.get("bytecode") as ByteArray,
                        locationId = it.get("location_id") as Long,
                        name = it.get("name_name") as String,
                        id = it.get("id") as Long
                    )
                }.also {
                    cursor.close()
                }
            }
        }

    }
}

private class ClassRecord(
    val byteCode: ByteArray,
    val locationId: Long,
    val name: String,
    val id: Long
)

suspend fun JcClasspath.hierarchyExt(): HierarchyExtensionImpl {
    db.awaitBackgroundJobs()
    return HierarchyExtensionImpl(this)
}

private class BatchedSequence(private val batchSize: Int, private val getNext: (Long?) -> List<ClassRecord>) : Sequence<ClassRecord> {

    private val result = arrayListOf<ClassRecord>()
    private var position = 0
    private var maxId: Long? = null

    override fun iterator(): Iterator<ClassRecord> {
        return object : Iterator<ClassRecord> {

            override fun hasNext(): Boolean {
                if (result.size == position && position % batchSize == 0) {
                    val incomingRecords = getNext(maxId)
                    if (incomingRecords.isEmpty()) {
                        return false
                    }
                    result.addAll(incomingRecords)
                    maxId = incomingRecords.maxOf { it.id }
                }
                return result.size > position
            }

            override fun next(): ClassRecord {
                position++
                return result[position - 1]
            }

        }
    }
}