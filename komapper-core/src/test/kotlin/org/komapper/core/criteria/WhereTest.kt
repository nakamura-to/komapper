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
        val a = Alias(0)
        val w = where {
            eq(a[Address::id], 1)
        }
        val criterionList = mutableListOf<Criterion>()
        WhereScope(a) { criterionList.add(it) }.w()
        assertEquals(listOf(Criterion.Eq(a[Address::id], 1)), criterionList)
    }

    @Test
    fun plus() {
        val a = Alias(0)
        val w1 = where {
            eq(a[Address::id], 1)
        }
        val w2 = where {
            ne(Address::street, "a")
        }
        val w3 = w1 + w2
        val criterionList = mutableListOf<Criterion>()
        WhereScope(a) { criterionList.add(it) }.w3()
        assertEquals(
            listOf(
                Criterion.Eq(a[Address::id], 1),
                Criterion.Ne(a[Address::street], "a")
            ),
            criterionList
        )
    }
}