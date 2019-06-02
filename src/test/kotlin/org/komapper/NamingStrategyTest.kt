package test.koma

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.NamingStrategy

class NamingStrategyTest {

    private val strategy = object : NamingStrategy {}

    @Test
    fun fromKotlinToDb() {
        assertEquals("", strategy.fromKotlinToDb(""))
        assertEquals("aaa_bbb_ccc", strategy.fromKotlinToDb("aaaBbbCcc"))
        assertEquals("abc", strategy.fromKotlinToDb("abc"))
        assertEquals("aa1_bbb_ccc", strategy.fromKotlinToDb("aa1BbbCcc"))
        assertEquals("sql", strategy.fromKotlinToDb("SQL"))
    }
}
