package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrderByCriteriaTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun test() {
        val criteria = orderBy {
            Address::id.desc()
        }
        val criterionList = mutableListOf<Pair<KProperty1<*, *>, String>>()
        OrderByScope { criterionList.add(it) }.criteria()
        assertEquals(listOf(Address::id to "desc"), criterionList)
    }

    @Test
    fun plus() {
        val criteria1 = orderBy {
            Address::id.desc()
        }
        val criteria2 = orderBy {
            Address::street.asc()
        }
        val criteria3 = criteria1 + criteria2
        val criterionList = mutableListOf<Pair<KProperty1<*, *>, String>>()
        OrderByScope { criterionList.add(it) }.criteria3()
        assertEquals(
            listOf(
                Address::id to "desc",
                Address::street to "asc"
            ),
            criterionList
        )
    }
}
