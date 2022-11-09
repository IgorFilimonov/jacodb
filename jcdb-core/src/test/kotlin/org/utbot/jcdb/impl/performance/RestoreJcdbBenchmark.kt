package org.utbot.jcdb.impl.performance

import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import org.utbot.jcdb.api.JCDB
import org.utbot.jcdb.impl.allClasspath
import org.utbot.jcdb.jcdb
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(0)
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class RestoreJcdbBenchmark {

    companion object {
        private val jdbcLocation = Files.createTempDirectory("jdbc-${UUID.randomUUID()}").toFile().absolutePath
    }

    var db: JCDB? = null

    @Setup
    fun setup() {
        val tempDb = newDB()
        tempDb.close()
    }

    @Benchmark
    fun restore() {
        db = newDB()
    }

    @TearDown(Level.Iteration)
    fun clean() {
        db?.close()
        db = null
    }

    private fun newDB(): JCDB {
        return runBlocking {
            jcdb {
                persistent(jdbcLocation)
                loadByteCode(allClasspath)
                useProcessJavaRuntime()
            }.also {
                it.awaitBackgroundJobs()
            }
        }
    }

}