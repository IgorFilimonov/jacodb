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

package org.jacodb.analysis.taint

import org.jacodb.analysis.ifds.Reason
import org.jacodb.api.common.CommonMethod
import org.jacodb.api.common.cfg.CommonInst

sealed interface TaintEvent<Method, Statement>
    where Method : CommonMethod<Method, Statement>,
          Statement : CommonInst<Method, Statement>

data class NewSummaryEdge<Method, Statement>(
    val edge: TaintEdge<Method, Statement>,
) : TaintEvent<Method, Statement>
    where Method : CommonMethod<Method, Statement>,
          Statement : CommonInst<Method, Statement>

data class NewVulnerability<Method, Statement>(
    val vulnerability: TaintVulnerability<Method, Statement>,
) : TaintEvent<Method, Statement>
    where Method : CommonMethod<Method, Statement>,
          Statement : CommonInst<Method, Statement>

data class EdgeForOtherRunner<Method, Statement>(
    val edge: TaintEdge<Method, Statement>,
    val reason: Reason<TaintDomainFact, Method, Statement>,
) : TaintEvent<Method, Statement>
    where Method : CommonMethod<Method, Statement>,
          Statement : CommonInst<Method, Statement> {

    init {
        // TODO: remove this check
        check(edge.from == edge.to) { "Edge for another runner must be a loop" }
    }
}
