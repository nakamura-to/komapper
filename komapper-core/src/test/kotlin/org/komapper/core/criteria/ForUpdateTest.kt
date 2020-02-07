package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ForUpdateTest {

    @Test
    fun test() {
        val f = forUpdate {
            nowait()
        }
        var criteria: ForUpdateCriteria? = null
        ForUpdateScope { criteria = it }.f()
        assertEquals(ForUpdateCriteria(true), criteria)
    }

    @Test
    fun plus() {
        val f1 = forUpdate {
            nowait()
        }
        val f2 = forUpdate {
            nowait(false)
        }
        val f3 = f1 + f2
        var criteria: ForUpdateCriteria? = null
        ForUpdateScope { criteria = it }.f3()
        assertEquals(ForUpdateCriteria(false), criteria)
    }
}
