package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrderByTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun test() {
        val o = orderBy {
            desc(Address::id)
        }
        val criterionList = mutableListOf<Pair<KProperty1<*, *>, String>>()
        OrderByScope { criterionList.add(it) }.o()
        assertEquals(listOf(Address::id to "desc"), criterionList)
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
        val criterionList = mutableListOf<Pair<KProperty1<*, *>, String>>()
        OrderByScope { criterionList.add(it) }.o3()
        assertEquals(
            listOf(
                Address::id to "desc",
                Address::street to "asc"
            ),
            criterionList
        )
    }
}
