package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WhereTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun test() {
        val w = where {
            eq(Address::id, 1)
        }
        val criterionList = mutableListOf<Criterion>()
        WhereScope { criterionList.add(it) }.w()
        assertEquals(listOf(Criterion.Eq(Address::id, 1)), criterionList)
    }

    @Test
    fun plus() {
        val w1 = where {
            eq(Address::id, 1)
        }
        val w2 = where {
            ne(Address::street, "a")
        }
        val w3 = w1 + w2
        val criterionList = mutableListOf<Criterion>()
        WhereScope { criterionList.add(it) }.w3()
        assertEquals(
            listOf(
                Criterion.Eq(Address::id, 1),
                Criterion.Ne(Address::street, "a")
            ),
            criterionList
        )
    }
}
