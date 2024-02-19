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

package org.jacodb.panda.dynamic.api

import org.jacodb.api.core.Project

class PandaProject(
    val classes: List<PandaClass>
) : Project<PandaType> {

    override fun findTypeOrNull(name: String): PandaType? {
        return null
    }

    fun findClassOrNull(name: String): PandaClass? = classes.find { it.name == name }

    fun findMethodOrNull(name: String, currentClassName: String): PandaMethod? =
        findClassOrNull(currentClassName)?.methods?.find {
            it.name == name
        }

    override fun close() {}

    companion object {

        fun empty(): PandaProject = PandaProject(emptyList())
    }

}

class PandaClass(
    val name: String,
    val methods: List<PandaMethod>
) {

    private var _project: PandaProject = PandaProject.empty()
    val project: PandaProject get() = _project

    fun setProject(value: PandaProject) {
        _project = value
    }
}
