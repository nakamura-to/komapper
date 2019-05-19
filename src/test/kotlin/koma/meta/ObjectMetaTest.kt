package koma.meta

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("UNUSED")
internal class ObjectMetaTest {

    @Test
    fun toMap() {
        val obj = object {
            val x = "a"
            val y = 1
        }
        val meta = ObjectMeta(obj::class)
        val map = meta.toMap(obj)
        assertEquals(mapOf("x" to ("a" to String::class), "y" to (1 to Int::class)), map)
    }

    @Test
    fun `exclude private properties`() {
        val obj = object {
            private val x = "a"
            val y = 1
        }
        val meta = ObjectMeta(obj::class)
        val map = meta.toMap(obj)
        assertEquals(mapOf("y" to (1 to Int::class)), map)
    }
}