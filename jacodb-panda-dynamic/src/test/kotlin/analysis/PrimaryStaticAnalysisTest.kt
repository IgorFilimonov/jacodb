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

package analysis

import org.jacodb.panda.dynamic.api.PandaAddExpr
import org.jacodb.panda.dynamic.api.PandaAnyType
import org.jacodb.panda.dynamic.api.PandaApplicationGraph
import org.jacodb.panda.dynamic.api.PandaApplicationGraphImpl
import org.jacodb.panda.dynamic.api.PandaArrayType
import org.jacodb.panda.dynamic.api.PandaAssignInst
import org.jacodb.panda.dynamic.api.PandaBinaryExpr
import org.jacodb.panda.dynamic.api.PandaBoolType
import org.jacodb.panda.dynamic.api.PandaCallExpr
import org.jacodb.panda.dynamic.api.PandaCallInst
import org.jacodb.panda.dynamic.api.PandaClassTypeImpl
import org.jacodb.panda.dynamic.api.PandaCmpExpr
import org.jacodb.panda.dynamic.api.PandaConditionExpr
import org.jacodb.panda.dynamic.api.PandaConstantWithValue
import org.jacodb.panda.dynamic.api.PandaDivExpr
import org.jacodb.panda.dynamic.api.PandaExpExpr
import org.jacodb.panda.dynamic.api.PandaInst
import org.jacodb.panda.dynamic.api.PandaInstanceCallExpr
import org.jacodb.panda.dynamic.api.PandaLoadedValue
import org.jacodb.panda.dynamic.api.PandaMethod
import org.jacodb.panda.dynamic.api.PandaModExpr
import org.jacodb.panda.dynamic.api.PandaMulExpr
import org.jacodb.panda.dynamic.api.PandaNullConstant
import org.jacodb.panda.dynamic.api.PandaNumberType
import org.jacodb.panda.dynamic.api.PandaPrimitiveType
import org.jacodb.panda.dynamic.api.PandaStringConstant
import org.jacodb.panda.dynamic.api.PandaStringType
import org.jacodb.panda.dynamic.api.PandaSubExpr
import org.jacodb.panda.dynamic.api.PandaUndefinedType
import org.jacodb.panda.dynamic.api.PandaValue
import org.jacodb.panda.dynamic.api.PandaValueByInstance
import org.jacodb.panda.dynamic.api.callExpr
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import parser.loadIr

private val logger = mu.KotlinLogging.logger {}

class PrimaryStaticAnalysisTest {
    private fun getOperands(inst: PandaInst): List<PandaValue> {
        return inst.operands.flatMap { expr -> expr.operands }
    }

    @Nested
    inner class ArgumentParameterCorrespondenceTest {

        private fun analyse(programName: String, startMethods: List<String>): List<Pair<PandaInst, PandaMethod>> {
            val parser = loadIr("/samples/${programName}.json")
            val project = parser.getProject()
            val graph = PandaApplicationGraphImpl(project)
            val methods = graph.project.classes.flatMap { it.methods }
            val mismatches = mutableListOf<Pair<PandaInst, PandaMethod>>()
            for (method in methods) {
                if (startMethods.contains(method.name)) {
                    for (inst in method.instructions) {
                        var callExpr: PandaCallExpr? = null
                        if (inst is PandaCallInst) {
                            callExpr = inst.callExpr
                        }
                        if (inst is PandaAssignInst) {
                            if (inst.rhv is PandaCallExpr) {
                                callExpr = inst.callExpr
                            }
                        }
                        if (callExpr == null) {
                            continue
                        }
                        val callee = callExpr.method

                        // TODO(): need more smart check that callee is not variadic function
                        if (callee.name == "log") {
                            continue
                        }
                        if (callExpr.args.size != callee.parameters.size) {
                            mismatches.add(Pair(inst, callee))
                            logger.info { "parameter-argument count mismatch for call: $inst (expected ${callee.parameters.size} arguments, but got ${callExpr.args.size})" }
                        }
                    }
                }
            }
            return mismatches
        }

        @Test
        fun `test for mismatch detection in regular function call`() {
            val mismatches = analyse(
                programName = "codeqlSamples/parametersArgumentsMismatch",
                startMethods = listOf("foo")
            )
            assert(mismatches.size == 1)
        }

        @Disabled("reconcile arguments number for class methods (is 'this' count?)")
        @Test
        fun `positive example - test for mismatch detection in class method call`() {
            val mismatches = analyse(
                programName = "codeqlSamples/parametersArgumentsMismatch",
                startMethods = listOf("rightUsage")
            )
            assert(mismatches.isEmpty())
        }

        @Disabled("Don't work cause we can't resolve constructors yet")
        @Test
        fun `counterexample - test for mismatch detection in class method call`() {
            val mismatches = analyse(
                programName = "codeqlSamples/parametersArgumentsMismatch",
                startMethods = listOf("wrongUsage")
            )
            assert(mismatches.size == 3)
        }
    }

    // TODO(): expand for writing (trysttoglobalbyname) and constants (stconsttoglobalbyname)
    @Nested
    inner class UnresolvedVariableTest {

        private val orderedInstructions = mutableListOf<PandaInst>()
        private fun dfs(inst: PandaInst, graph: PandaApplicationGraph, visited: MutableMap<PandaInst, Boolean>) {
            visited[inst] = true
            for (successorInst in graph.successors(inst)) {
                if (!visited.getOrDefault(successorInst, false)) {
                    dfs(successorInst, graph, visited)
                }
            }
            for (callee in graph.callees(inst)) {
                val firstInstInCallee = callee.instructions.first()
                if (!visited.getOrDefault(firstInstInCallee, false)) {
                    dfs(firstInstInCallee, graph, visited)
                }
            }
            orderedInstructions.add(inst)
        }

        private fun topologicalSort(graph: PandaApplicationGraph) {
            orderedInstructions.clear()
            val visited = mutableMapOf<PandaInst, Boolean>()
            val methods = graph.project.classes.flatMap { it.methods }
            val startInst = methods.filter { it.name == "func_main_0" }.single().instructions.first()
            dfs(startInst, graph, visited)
            for (method in methods) {
                val firstInst = method.instructions.first()
                if (!visited.getOrDefault(startInst, false)) {
                    dfs(firstInst, graph, visited)
                }
            }
            orderedInstructions.reverse()
        }

        private fun analyse(programName: String, startMethods: List<String>? = null): List<Pair<String, PandaInst>> {
            val parser = loadIr("/samples/${programName}.json")
            val project = parser.getProject()
            val graph = PandaApplicationGraphImpl(project)

            topologicalSort(graph)

            val instToGlobalVars = mutableMapOf<PandaInst, MutableSet<String>>()

            val unresolvedVariables = mutableListOf<Pair<String, PandaInst>>()

            for (inst in orderedInstructions) {
                var predecessorInstructions = mutableListOf<PandaInst>()
                if (inst.location.index == 0) {
                    predecessorInstructions.addAll(graph.callers(inst.location.method))
                }
                predecessorInstructions.addAll(graph.predecessors(inst).toList())
                if (predecessorInstructions.isEmpty()) {
                    instToGlobalVars[inst] = mutableSetOf()
                } else {
                    instToGlobalVars[inst] = instToGlobalVars[predecessorInstructions.first()]!!.toMutableSet()
                }
                for (predecessorInst in graph.predecessors(inst).drop(1)) {
                    instToGlobalVars[inst]!!.intersect(instToGlobalVars[predecessorInst]!!)
                }
                if (inst is PandaAssignInst && inst.varName != null) {
                    instToGlobalVars[inst]!!.add(inst.varName!!)
                }
                // adhoc check for tryldglobalname, TODO(): check trysttoglobalname (for both will be cooler after better global variable processing)
                val probablyUndefinedVarNames = getOperands(inst).mapNotNull { op ->
                    if (op is PandaLoadedValue && op.instance is PandaStringConstant) {
                        ((op.instance) as PandaStringConstant).value
                    } else null
                }

                val stdVarNames = listOf("console") // TODO(): need more smart filter
                probablyUndefinedVarNames.forEach { varName ->
                    if (varName !in stdVarNames && varName !in instToGlobalVars[inst]!!) {
                        unresolvedVariables.add(Pair(varName, inst))
                        logger.info { "unresolved variable $varName in $inst with location: (method:${inst.location.method}, index: ${inst.location.index})" }
                    }
                }
            }

            return unresolvedVariables
        }

        @Test
        fun `counterexample - program that read some unresolved variables`() {
            val unresolvedVariables = analyse(
                programName = "codeqlSamples/unresolvedVariable",
            )
            assert(unresolvedVariables.size == 4)
        }
    }

    enum class ImplicitCastAnalysisMode {
        DETECTION,
        POSSIBILITY_CHECK
    }

    @Nested
    inner class ImplicitCastingTest {
        private fun analyse(
            programName: String,
            startMethods: List<String>,
            mode: ImplicitCastAnalysisMode = ImplicitCastAnalysisMode.DETECTION,
        ): List<PandaInst> {
            val parser = loadIr("/samples/${programName}.json")
            val project = parser.getProject()
            val graph = PandaApplicationGraphImpl(project)
            val methods = graph.project.classes.flatMap { it.methods }
            val typeMismatches = mutableListOf<PandaInst>()
            for (method in methods) {
                if (startMethods.contains(method.name)) {
                    for (inst in method.instructions) {
                        if (inst is PandaAssignInst && inst.rhv is PandaBinaryExpr) {
                            val operation = inst.rhv
                            val (leftOp, rightOp) = operation.operands
                            // TODO(): extend analysis for more complex scenarios
                            if (leftOp.type == PandaAnyType || rightOp.type == PandaAnyType) {
                                continue
                            }
                            if (mode == ImplicitCastAnalysisMode.DETECTION) {
                                if (leftOp.type != rightOp.type) {
                                    logger.info { "implicit casting in: $inst ($leftOp has ${leftOp.type} type, but $rightOp has ${rightOp.type} type)" }
                                    typeMismatches.add(inst)
                                }
                            }
                            if (mode == ImplicitCastAnalysisMode.POSSIBILITY_CHECK) {
                                val fineOperations = listOf(
                                    PandaAddExpr::class,
                                    PandaCmpExpr::class,
                                    PandaConditionExpr::class
                                )
                                val numberOperations = listOf(
                                    PandaSubExpr::class,
                                    PandaDivExpr::class,
                                    PandaModExpr::class,
                                    PandaExpExpr::class,
                                    PandaMulExpr::class
                                )
                                val isNumeric: (Any) -> Boolean = { value ->
                                    when (value) {
                                        is String -> value.toDoubleOrNull() != null
                                        is Int -> true
                                        else -> throw IllegalArgumentException("Unexpected type")
                                    }
                                }
                                val typePriority = mapOf(
                                    PandaStringType::class to 0,
                                    PandaNumberType::class to 1,
                                    PandaBoolType::class to 2
                                )
                                val sortedOps = listOf(leftOp, rightOp)
                                sortedOps.sortedBy { elem ->
                                    typePriority[elem.type::class]
                                }
                                if (numberOperations.any { it.isInstance(operation) }) {
                                    if (sortedOps[0] is PandaConstantWithValue && sortedOps[1] is PandaConstantWithValue) {
                                        if (!isNumeric((sortedOps[0] as PandaConstantWithValue).value!!) || !isNumeric((sortedOps[1] as PandaConstantWithValue).value!!)) {
                                            logger.info { "implicit cast won't work in: $inst (both operands should implicitly cast to number)" }
                                            typeMismatches.add(inst)
                                        } else {
                                            logger.info { "successful implicit cast in: $inst (both operands implicitly cast to number)" }
                                        }
                                    } else {
                                        TODO("Extend on constants with no value!")
                                    }
                                } else {
                                    logger.info { "implicit cast is not needed in: $inst" }
                                }
                            }
                        }
                    }
                }
            }
            return typeMismatches
        }

        @Test
        fun `test implicit casting observation in binary expressions with primitive literals`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/implicitCasting",
                startMethods = listOf("primitiveLiterals")
            )
            assert(typeMismatches.size == 10)
        }

        @Disabled("No type support for arrays and objects")
        @Test
        fun `test implicit casting observation in binary expressions with complex literals`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/implicitCasting",
                startMethods = listOf("complexLiterals")
            )
            assert(typeMismatches.size == 4)
        }

        @Test
        fun `test implicit casting checking in binary expressions with strings`() {
            val impossibleImplicitCasts = analyse(
                programName = "codeqlSamples/implicitCasting",
                startMethods = listOf("binaryOperationsWithStrings"),
                mode = ImplicitCastAnalysisMode.POSSIBILITY_CHECK
            )
            assert(impossibleImplicitCasts.size == 8)
        }

        @Disabled("Not supported yet")
        @Test
        fun `complicate test implicit casting checking in binary expressions with strings`() {
            val impossibleImplicitCasts = analyse(
                programName = "codeqlSamples/implicitCasting",
                startMethods = listOf("binaryOperationsWithStrings2"),
                mode = ImplicitCastAnalysisMode.POSSIBILITY_CHECK
            )
            assert(impossibleImplicitCasts.size == 2)
        }
    }

    @Nested
    inner class MissingMembersTest {
        private val noMemberTypes = listOf(
            PandaUndefinedType::class,
            PandaArrayType::class,
            PandaPrimitiveType::class
        )

        private fun analyse(
            programName: String,
            startMethods: List<String>,
            mode: ImplicitCastAnalysisMode = ImplicitCastAnalysisMode.DETECTION,
        ): List<PandaInst> {
            val parser = loadIr("/samples/${programName}.json")
            val project = parser.getProject()
            val graph = PandaApplicationGraphImpl(project)
            val methods = graph.project.classes.flatMap { it.methods }
            val typeMismatches = mutableListOf<PandaInst>()
            for (method in methods) {
                if (startMethods.contains(method.name)) {
                    for (inst in method.instructions) {
                        val probablyMissingPropertyMembers = getOperands(inst).mapNotNull { op ->
                            when (op) {
                                is PandaValueByInstance -> op
                                else -> null
                            }
                        }

                        probablyMissingPropertyMembers.forEach { member ->
                            if (noMemberTypes.any { it.isInstance(member.instance.type) }) {
                                logger.info { "Accessing member ${member.property} on instance ${member.instance} of ${member.instance.typeName} type (inst: $inst)" }
                                typeMismatches.add(inst)
                            } else if (member.instance is PandaNullConstant) {
                                logger.info { "Accessing member ${member.property} on instance ${member.instance} (inst: $inst)" }
                                typeMismatches.add(inst)
                            }
                        }

                        var callExpr: PandaInstanceCallExpr? = null

                        if (inst is PandaCallInst) {
                            if (inst.callExpr !is PandaInstanceCallExpr) {
                                TODO("Consider that case too")
                            }
                            callExpr = inst.callExpr as PandaInstanceCallExpr
                        }
                        if (inst is PandaAssignInst) {
                            inst.rhv.let { right ->
                                if (right is PandaCallExpr) {
                                    when (right) {
                                        is PandaInstanceCallExpr -> callExpr = right
                                        else -> TODO("Consider that case too")
                                    }
                                }
                            }
                        }
                        if (callExpr == null) {
                            continue
                        }
                        val callee = callExpr!!.method
                        val instance = callExpr!!.instance
                        if (noMemberTypes.any { it.isInstance(instance.type) }) {
                            logger.info { "Calling member ${callee.name} on instance $instance of ${instance.typeName} type (inst: $inst)" }
                            typeMismatches.add(inst)
                        } else if (instance is PandaNullConstant) {
                            logger.info { "Calling member ${callee.name} on instance $instance (inst: $inst)" }
                            typeMismatches.add(inst)
                        } else if (instance.type is PandaClassTypeImpl || instance is PandaLoadedValue) {
                            try {
                                // TODO(): get rid off adhoc
                                if (instance is PandaLoadedValue && instance.className == "console" && callee.name == "log") {
                                    continue
                                }
                                // TODO: "callee.enclosingClass" is always non-null, BUT can be non-initialized (lateinit var), which will cause an exception in runtime
                                if (callee.enclosingClass != null) {
                                    continue
                                }
                            } catch (e: UninitializedPropertyAccessException) { // simply means that IRParser cannot resolve a method
                                logger.info { "Calling member ${callee.name} on instance $instance that have no such a member (inst: $inst)" }
                                typeMismatches.add(inst)
                            }
                        }
                    }
                }
            }
            return typeMismatches
        }

        @Test
        fun `test calling members on primitive types`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/missingMembers",
                startMethods = listOf("callingMembersOnPrimitiveTypes")
            )
            assert(typeMismatches.size == 6)
        }

        @Disabled("createarraywithbuffer not supported yet cause have no information about its elements in IR")
        @Test
        fun `test calling members on non object types`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/missingMembers",
                startMethods = listOf("callingMembersOnNonObjectsTypes")
            )
            assert(typeMismatches.size == 4)
        }

        @Test
        fun `test calling members on null`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/missingMembers",
                startMethods = listOf("callingMembersOnNullType")
            )
            assert(typeMismatches.size == 2)
        }

        @Disabled("objects weakly supported right now both in IR and in parser")
        @Test
        fun `test calling members on objects`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/missingMembers",
                startMethods = listOf("callingMembersOnObjects")
            )
            assert(typeMismatches.isEmpty())
        }

        @Test
        fun `test calling methods on class instance`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/missingMembers2",
                startMethods = listOf("callingMethods")
            )
            assert(typeMismatches.size == 1)
        }

        @Disabled("IRParser doesn't track information about properties and IR have no information about uninitialized properties")
        @Test
        fun `test accessing properties on class instance`() {
            val typeMismatches = analyse(
                programName = "codeqlSamples/missingMembers2",
                startMethods = listOf("accessingProperties")
            )
            assert(typeMismatches.size == 4)
        }

        @Disabled("IR have no information about superclasses so lack knowledge about not overrided members")
        @Test
        fun `test on child instance calling methods defined in parent class and not overrided after that in child`() {
            val typeMismatches = analyse(
                programName = "classes/InheritanceClass",
                startMethods = listOf("func_main_0")
            )
            assert(typeMismatches.isEmpty())
        }

        @Test
        fun `test calling static methods`() {
            val typeMismatches = analyse(
                programName = "classes/StaticClass",
                startMethods = listOf("func_main_0")
            )
            assert(typeMismatches.isEmpty())
        }
    }
}
