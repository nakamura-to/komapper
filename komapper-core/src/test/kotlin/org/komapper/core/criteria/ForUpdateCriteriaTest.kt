package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ForUpdateCriteriaTest {

    @Test
    fun test() {
        val criteria = forUpdate {
            nowait()
        }
        var forUpdate: ForUpdate? = null
        ForUpdateScope { forUpdate = it }.criteria()
        assertEquals(ForUpdate(true), forUpdate)
    }

    @Test
    fun plus() {
        val criteria1 = forUpdate {
            nowait()
        }
        val criteria2 = forUpdate {
            nowait(false)
        }
        val criteria3 = criteria1 + criteria2
        var forUpdate: ForUpdate? = null
        ForUpdateScope { forUpdate = it }.criteria3()
        assertEquals(ForUpdate(false), forUpdate)
    }
}
