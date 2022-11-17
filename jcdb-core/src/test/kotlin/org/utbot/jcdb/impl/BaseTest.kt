package org.utbot.jcdb.impl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.utbot.jcdb.api.JCDB
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.JcFeature
import org.utbot.jcdb.jcdb
import java.nio.file.Files
import kotlin.reflect.full.companionObjectInstance

@ExtendWith(CleanDB::class)
abstract class BaseTest {

    protected val cp: JcClasspath = runBlocking {
        val withDB = this@BaseTest.javaClass.withDB
        withDB.db.classpath(allClasspath)
    }

    @AfterEach
    fun close() {
        cp.close()
    }

}

val Class<*>.withDB: WithDB
    get() {
        val comp = kotlin.companionObjectInstance
        if (comp is WithDB) {
            return comp
        }
        val s = superclass
        if (superclass == null) {
            throw IllegalStateException("can't find WithDB companion object. Please check that test class has it.")
        }
        return s.withDB
    }

open class WithDB(vararg features: JcFeature<*, *>) {

    protected var allFeatures = features.toList().toTypedArray()

    open var db = runBlocking {
        jcdb {
            loadByteCode(allClasspath)
            useProcessJavaRuntime()
            installFeatures(*allFeatures)
        }.also {
            it.awaitBackgroundJobs()
        }
    }

    open fun cleanup() {
        db.close()
    }
}

open class WithRestoredDB(vararg features: JcFeature<*, *>) : WithDB(*features) {

    private val jdbcLocation = Files.createTempFile("jcdb-", null).toFile().absolutePath

    var tempDb: JCDB? = newDB()

    override var db: JCDB = newDB {
        tempDb?.close()
        tempDb = null
    }

    private fun newDB(before: () -> Unit = {}): JCDB {
        before()
        return runBlocking {
            jcdb {
                persistent(jdbcLocation)
                loadByteCode(allClasspath)
                useProcessJavaRuntime()
                installFeatures(*allFeatures)
            }.also {
                it.awaitBackgroundJobs()
            }
        }
    }

}


class CleanDB : AfterAllCallback {
    override fun afterAll(context: ExtensionContext) {
        val companion = context.requiredTestClass.kotlin.companionObjectInstance
        if (companion is WithDB) {
            companion.cleanup()
        }
    }
}
