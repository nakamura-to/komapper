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
        val e = Alias()
        val j: Join<Employee, Address> = { a ->
            eq(e[Employee::addressId], a[Address::id])
        }
        val criterionList = mutableListOf<Criterion>()
        val scope = JoinScope<Employee, Address>({ criterionList.add(it) }, {})
        val a = e.next()
        j(scope, a)
        assertEquals(listOf(Criterion.Eq(e[Employee::addressId], a[Address::id])), criterionList)
    }

    @Test
    fun plus() {
        val e = Alias()
        val j1: Join<Employee, Address> = { a ->
            eq(e[Employee::id], a[Address::id])
        }
        val j2: Join<Employee, Address> = { a ->
            ne(e[Employee::id], a[Address::id])
        }
        val j3 = j1 + j2
        val criterionList = mutableListOf<Criterion>()
        val scope = JoinScope<Employee, Address>({ criterionList.add(it) }, {})
        val a = e.next()
        scope.j3(a)
        assertEquals(
            listOf(
                Criterion.Eq(e[Employee::id], a[Address::id]),
                Criterion.Ne(e[Employee::id], a[Address::id])
            ),
            criterionList
        )
    }
}
