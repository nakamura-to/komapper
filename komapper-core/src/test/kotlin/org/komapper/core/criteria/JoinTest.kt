package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JoinTest {

    private data class Employee(
        val id: Int,
        val addressId: Int
    )

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun test() {
        val j = join<Employee, Address> {
            eq(Employee::addressId, Address::id)
        }
        val criterionList = mutableListOf<Criterion>()
        val scope = JoinScope<Employee, Address>({ criterionList.add(it) }, {})
        j(scope)
        assertEquals(listOf(Criterion.Eq(Employee::addressId, Address::id)), criterionList)
    }

    @Test
    fun plus() {
        val j1 = join<Employee, Address> {
            eq(Employee::id, Address::id)
        }
        val j2 = join<Employee, Address>() {
            ne(Employee::id, Address::id)
        }
        val j3 = j1 + j2
        val criterionList = mutableListOf<Criterion>()
        val scope = JoinScope<Employee, Address>({ criterionList.add(it) }, {})
        scope.j3()
        assertEquals(
            listOf(
                Criterion.Eq(Employee::id, Address::id),
                Criterion.Ne(Employee::id, Address::id)
            ),
            criterionList
        )
    }
}
