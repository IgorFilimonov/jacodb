package org.utbot.jcdb.impl.features

import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnJre
import org.junit.jupiter.api.condition.JRE
import org.utbot.jcdb.api.JcMethod
import org.utbot.jcdb.api.ext.findClass
import org.utbot.jcdb.impl.BaseTest
import org.utbot.jcdb.impl.WithDB
import org.utbot.jcdb.impl.builders.Hierarchy.HierarchyInterface
import org.utbot.jcdb.impl.builders.Interfaces.Interface
import org.utbot.jcdb.impl.builders.Simple
import javax.xml.parsers.DocumentBuilderFactory

class BuildersTest : BaseTest() {

    companion object : WithDB(InMemoryHierarchy, Builders)

    private val ext = runBlocking {
        cp.buildersExtension()
    }

    @Test
    fun `simple find builders`() {
        val builders = ext.findBuildMethods(cp.findClass<Simple>()).toList()
        assertEquals(1, builders.size)
        assertEquals("build", builders.first().name)
    }

    @Test
    fun `java package is not indexed`() {
        val builders = ext.findBuildMethods(cp.findClass<ArrayList<*>>())
        assertFalse(builders.iterator().hasNext())
    }

    @Test
    fun `method parameters is took into account`() {
        val builders = ext.findBuildMethods(cp.findClass<Interface>()).toList()
        assertEquals(1, builders.size)
        assertEquals("build1", builders.first().name)
    }

    @Test
    @DisabledOnJre(JRE.JAVA_8)
    fun `works for DocumentBuilderFactory`() {
        val builders = ext.findBuildMethods(cp.findClass<DocumentBuilderFactory>()).toList()
        val expected = builders.map { it.loggable }
        assertTrue(expected.contains("javax.xml.parsers.DocumentBuilderFactory#newDefaultInstance"))
        assertTrue(expected.contains("javax.xml.parsers.DocumentBuilderFactory#newInstance"))
    }

    @Test
    fun `works for DocumentBuilderFactory for java 8`() {
        val builders = ext.findBuildMethods(cp.findClass<DocumentBuilderFactory>()).toList()
        val expected = builders.map { it.loggable }
        assertTrue(expected.contains("javax.xml.parsers.DocumentBuilderFactory#newInstance"))
    }

    @Test
    fun `works for jooq`() {
        val builders = ext.findBuildMethods(cp.findClass<DSLContext>()).toList()
        assertEquals("org.jooq.impl.DSL#using", builders.first().loggable)
    }

    @Test
    fun `works for methods returns subclasses`() {
        val builders = ext.findBuildMethods(cp.findClass<HierarchyInterface>(), includeSubclasses = true).toList()
        assertEquals(1, builders.size)
        assertEquals("org.utbot.jcdb.impl.builders.Hierarchy#build", builders.first().loggable)
    }

    private val JcMethod.loggable get() = enclosingClass.name + "#" + name
}