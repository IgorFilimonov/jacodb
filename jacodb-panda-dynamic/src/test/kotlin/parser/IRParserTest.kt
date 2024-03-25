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

package parser

import org.jacodb.panda.dynamic.parser.ByteCodeParser
import org.jacodb.panda.dynamic.parser.IRParser
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertEquals

private val logger = mu.KotlinLogging.logger {}

class IRParserTest {
    private fun loadIR(fileName: String = "TypeMismatch"): IRParser {
        val bcParser = javaClass.getResource("/samples/$fileName.abc")?.path?.let { FileInputStream(it).readBytes() }
            ?.let { ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN) }
            ?.let { ByteCodeParser(it) }
            ?.also { it.parseABC() }
        val sampleFilePath = javaClass.getResource("/samples/$fileName.json")?.path ?: ""
        return IRParser(sampleFilePath, bcParser!!)
    }

    @Test
    fun getProject() {
        val irParser = loadIR()
        val pandaProject = irParser.getProject()
        assertNotNull(pandaProject)
    }

    @Test
    fun getProgramIR() {
        val irParser = loadIR()
        val programIR = irParser.getProgramIR()
        val classes = programIR.classes
        logger.info { "Classes name: ${classes.joinToString(separator = ", ") { it.name }}" }
        logger.info { "Methods name: ${classes.flatMap { it.methods }.joinToString(separator = ", ") { it.name }}" }
        assertNotNull(programIR)
    }

    @Test
    fun getPandaMethods() {
        val irParser = loadIR()
        val programIR = irParser.getProgramIR()
        programIR.classes.forEach { cls ->
            cls.methods.forEach { method ->
                val pandaMethod = method.pandaMethod
                assertNotNull(pandaMethod.name)
                assertNotNull(pandaMethod.instructions)
                logger.info { "Panda method '${pandaMethod.name}', instructions: ${pandaMethod.instructions}" }

            }
        }
    }

    @Test
    fun getSetOfProgramOpcodes() {
        val irParser = loadIR()
        val programIR = irParser.getProgramIR()
        val opcodes = programIR.classes.asSequence().flatMap { it.methods }
            .flatMap { it.basicBlocks }
            .flatMap { it.insts }
            .map { it.opcode }
            .toSortedSet()
        assertNotNull(opcodes)
    }

    @Test
    fun printMethodsInstructions() {
        val irParser = loadIR()
        val programIR = irParser.getProgramIR()
        programIR.classes.forEach { cls ->
            cls.methods.forEach { method ->
                println(method)
            }
        }
    }

    @Test
    fun `test parser on TypeMismatch`() {
        val irParser = loadIR("TypeMismatch")
        val programIR = irParser.getProgramIR()
        programIR.classes.forEach { cls ->
            cls.methods.forEach { method ->
                val pandaMethod = method.pandaMethod
                assertNotNull(pandaMethod.name)
                assertNotNull(pandaMethod.instructions)
                when (pandaMethod.name) {
                    "add" -> {
                        assertEquals(9, pandaMethod.instructions.size)
                        assertEquals(4, pandaMethod.blocks.size)
                    }

                    "main" -> {
                        assertEquals(3, pandaMethod.instructions.size)
                        assertEquals(2, pandaMethod.blocks.size)
                    }
                }
            }
        }
    }
}
