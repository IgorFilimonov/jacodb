package org.utbot.jcdb.impl.bytecode

import org.utbot.jcdb.api.ClassSource
import org.utbot.jcdb.api.JcClassOrInterface
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.JcMethod
import org.utbot.jcdb.impl.types.MethodInfo
import org.utbot.jcdb.impl.vfs.ClassVfsItem

fun JcClasspath.toJcClass(item: ClassVfsItem?): JcClassOrInterface? {
    item ?: return null
    return toJcClass(item.source)
}

fun JcClassOrInterface.toJcMethod(methodInfo: MethodInfo, source: ClassSource): JcMethod {
    return JcMethodImpl(methodInfo, source, this)
}