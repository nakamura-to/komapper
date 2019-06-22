package org.komapper.meta

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.Value

@Suppress("UNUSED")
internal class DefaultObjectMetaFactoryTest {

    private val objectMetaFactory = DefaultObjectMetaFactory()

    @Test
    fun toMap() {
        val obj = object {
            val x = "a"
            val y = 1
        }
        val map = objectMetaFactory.toMap(obj)
        assertEquals(mapOf("x" to Value("a", String::class), "y" to Value(1, Int::class)), map)
    }

    @Test
    fun `exclude private properties`() {
        val obj = object {
            private val x = "a"
            val y = 1
        }
        val map = objectMetaFactory.toMap(obj)
        assertEquals(mapOf("y" to Value(1, Int::class)), map)
    }
}
