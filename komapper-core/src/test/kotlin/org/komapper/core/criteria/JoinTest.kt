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
        val e = Alias(0)
        val j = join<Employee, Address> { a ->
            eq(e[Employee::addressId], a[Address::id])
        }
        val criterionList = mutableListOf<Criterion>()
        val scope = JoinScope<Employee, Address>(e, { criterionList.add(it) }, {})
        val a = Alias(1)
        j(scope, a)
        assertEquals(listOf(Criterion.Eq(e[Employee::addressId], a[Address::id])), criterionList)
    }

    @Test
    fun plus() {
        val e = Alias(0)
        val j1 = join<Employee, Address> { a ->
            eq(e[Employee::id], a[Address::id])
        }
        val j2 = join<Employee, Address> { a ->
            ne(e[Employee::id], a[Address::id])
        }
        val j3 = j1 + j2
        val criterionList = mutableListOf<Criterion>()
        val scope = JoinScope<Employee, Address>(e, { criterionList.add(it) }, {})
        val a = Alias(1)
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
