package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WhereCriteriaTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun test() {
        val criteria = where {
            Address::id.eq(1)
        }
        val criterionList = mutableListOf<Criterion>()
        WhereScope { criterionList.add(it) }.criteria()
        assertEquals(listOf(Criterion.Eq(Address::id, 1)), criterionList)
    }

    @Test
    fun plus() {
        val criteria1 = where {
            Address::id.eq(1)
        }
        val criteria2 = where {
            Address::street.ne("a")
        }
        val criteria3 = criteria1 + criteria2
        val criterionList = mutableListOf<Criterion>()
        WhereScope { criterionList.add(it) }.criteria3()
        assertEquals(
            listOf(
                Criterion.Eq(Address::id, 1),
                Criterion.Ne(Address::street, "a")
            ),
            criterionList
        )
    }
}
