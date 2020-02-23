package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrderByTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun test() {
        val a = Alias()
        val o = orderBy {
            desc(a[Address::id])
        }
        val criterionList = mutableListOf<OrderByItem>()
        OrderByScope() { criterionList.add(it) }.o()
        assertEquals(listOf(OrderByItem(a[Address::id], "desc")), criterionList)
    }

    @Test
    fun plus() {
        val o1 = orderBy {
            desc(Address::id)
        }
        val o2 = orderBy {
            asc(Address::street)
        }
        val o3 = o1 + o2
        val criterionList = mutableListOf<OrderByItem>()
        OrderByScope() { criterionList.add(it) }.o3()
        assertEquals(
            listOf(
                OrderByItem(Expression.wrap(Address::id), "desc"),
                OrderByItem(Expression.wrap(Address::street), "asc")
            ),
            criterionList
        )
    }
}
