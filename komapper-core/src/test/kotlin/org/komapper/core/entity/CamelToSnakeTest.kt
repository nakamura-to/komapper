package org.komapper.core.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CamelToSnakeTest {

    private val strategy = CamelToSnake()

    @Test
    fun fromKotlinToDb() {
        assertEquals("", strategy.fromKotlinToDb(""))
        assertEquals("aaa_bbb_ccc", strategy.fromKotlinToDb("aaaBbbCcc"))
        assertEquals("abc", strategy.fromKotlinToDb("abc"))
        assertEquals("aa1_bbb_ccc", strategy.fromKotlinToDb("aa1BbbCcc"))
        assertEquals("sql", strategy.fromKotlinToDb("SQL"))
    }
}
