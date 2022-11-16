package org.utbot.jcdb.impl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.utbot.jcdb.api.JcClassOrInterface
import org.utbot.jcdb.impl.features.InMemoryHierarchy
import org.utbot.jcdb.impl.features.findSubclassesInMemory
import org.w3c.dom.Document
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class BaseInMemoryHierarchyTest : BaseTest() {

    @Test
    fun `find subclasses for class`() {
        with(findSubClasses<AbstractMap<*, *>>(allHierarchy = true).toList()) {
            assertTrue(size > 10) {
                "expected more then 10 but got only: ${joinToString { it.name }}"
            }

            assertNotNull(firstOrNull { it.name == EnumMap::class.java.name })
            assertNotNull(firstOrNull { it.name == HashMap::class.java.name })
            assertNotNull(firstOrNull { it.name == WeakHashMap::class.java.name })
            assertNotNull(firstOrNull { it.name == TreeMap::class.java.name })
            assertNotNull(firstOrNull { it.name == ConcurrentHashMap::class.java.name })
        }
    }

    @Test
    fun `find subclasses for interface`() {
        with(findSubClasses<Document>()) {
            assertTrue(count() > 0)
        }
    }

    @Test
    fun `find huge number of subclasses`() {
        with(findSubClasses<Runnable>()) {
            assertTrue(count() > 10)
        }
    }

    private inline fun <reified T> findSubClasses(allHierarchy: Boolean = false): Sequence<JcClassOrInterface> =
        runBlocking {
            cp.findSubclassesInMemory(T::class.java.name, allHierarchy)
        }

}

class InMemoryHierarchyTest : BaseInMemoryHierarchyTest() {
    companion object : WithDB(InMemoryHierarchy)
}

class RestoredInMemoryHierarchyTest : BaseInMemoryHierarchyTest() {
    companion object : WithRestoredDB(InMemoryHierarchy)
}