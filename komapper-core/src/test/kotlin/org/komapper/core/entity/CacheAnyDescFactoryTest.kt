package org.komapper.core.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.sql.CacheAnyDescFactory
import org.komapper.core.value.Value

@Suppress("UNUSED")
internal class CacheAnyDescFactoryTest {

    private val objectMetaFactory = CacheAnyDescFactory()

    @Test
    fun toMap() {
        val obj = object {
            val x = "a"
            val y = 1
        }
        val map = objectMetaFactory.toMap(obj)
        assertEquals(
            mapOf(
                "x" to Value("a", String::class), "y" to Value(
                    1,
                    Int::class
                )
            ), map
        )
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
