/*
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jacodb.testing

import kotlinx.coroutines.runBlocking
import org.jacodb.api.jvm.JcClasspath
import org.jacodb.api.jvm.ext.CONSTRUCTOR
import org.jacodb.api.jvm.ext.findClass
import org.jacodb.api.jvm.ext.usedFields
import org.jacodb.api.jvm.ext.usedMethods
import org.jacodb.testing.usages.direct.DirectA
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectUsagesTest : BaseTest() {

    companion object : WithGlobalDB()

    @Test
    fun `find methods used in method`() {
        val usages = cp.methodsUsages<DirectA>()

        assertEquals(
            listOf(
                CONSTRUCTOR to listOf("java.lang.Object#<init>"),
                "setCalled" to listOf(
                    "java.io.PrintStream#println",
                ),
                "newSmth" to listOf(
                    "com.google.common.collect.Lists#newArrayList",
                    "java.lang.Integer#valueOf",
                    "java.util.ArrayList#add",
                    "java.io.PrintStream#println",
                )
            ).sortedBy { it.first },
            usages
        )
    }

    @Test
    fun `find methods used in method with broken classpath`() {
        val cp = runBlocking {
            db.classpath(allClasspath - guavaLib)
        }
        cp.use {
            val usages = cp.methodsUsages<DirectA>()

            assertEquals(
                listOf(
                    CONSTRUCTOR to listOf("java.lang.Object#<init>"),
                    "setCalled" to listOf(
                        "java.io.PrintStream#println",
                    ),
                    "newSmth" to listOf(
                        "java.lang.Integer#valueOf",
                        "java.util.ArrayList#add",
                        "java.io.PrintStream#println",
                    )
                ).sortedBy { it.first },
                usages
            )
        }
    }

    @Test
    fun `find fields used in method`() {
        val usages = cp.fieldsUsages<DirectA>()

        assertEquals(
            listOf(
                CONSTRUCTOR to listOf(
                    "reads" to listOf(),
                    "writes" to listOf()
                ),
                "newSmth" to listOf(
                    "reads" to listOf(
                        "java.lang.System#out",
                        "org.jacodb.testing.usages.direct.DirectA#called",
                        "org.jacodb.testing.usages.direct.DirectA#result",
                    ),
                    "writes" to listOf(
                        "org.jacodb.testing.usages.direct.DirectA#called",
                        "org.jacodb.testing.usages.direct.DirectA#result",
                    )
                ),
                "setCalled" to listOf(
                    "reads" to listOf(
                        "java.lang.System#out",
                        "org.jacodb.testing.usages.direct.DirectA#called",
                    ),
                    "writes" to listOf(
                        "org.jacodb.testing.usages.direct.DirectA#called",
                    )
                )
            ).sortedBy { it.first },
            usages
        )
    }

    private inline fun <reified T> JcClasspath.fieldsUsages(): List<Pair<String, List<Pair<String, List<String>>>>> {
        return runBlocking {
            val classId = findClass<T>()

            classId.declaredMethods.map {
                val usages = it.usedFields
                it.name to listOf(
                    "reads" to usages.reads.map { it.enclosingClass.name + "#" + it.name }.sortedBy { it },
                    "writes" to usages.writes.map { it.enclosingClass.name + "#" + it.name }.sortedBy { it }
                )
            }
                .toMap()
                .filterNot { it.value.isEmpty() }
                .toSortedMap().toList()
        }
    }

    private inline fun <reified T> JcClasspath.methodsUsages(): List<Pair<String, List<String>>> {
        return runBlocking {
            val jcClass = findClass<T>()

            val methods = jcClass.declaredMethods

            methods.map {
                it.name to it.usedMethods.map { it.enclosingClass.name + "#" + it.name }
            }.filterNot { it.second.isEmpty() }.sortedBy { it.first }
        }
    }

    @AfterEach
    fun cleanup() {
        cp.close()
    }

}
