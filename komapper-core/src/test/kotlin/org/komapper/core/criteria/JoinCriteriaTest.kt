package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JoinCriteriaTest {

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
        val criteria = join<Employee, Address> {
            Employee::addressId.eq(Address::id)
        }
        val join = Join<Employee, Address>(JoinKind.INNER, Address::class).also {
            val scope = JoinScope(it)
            criteria(scope)
        }
        assertEquals(listOf(Criterion.Eq(Employee::addressId, Address::id)), join.on)
    }

    @Test
    fun plus() {
        val criteria1 = join<Employee, Address> {
            Employee::id.eq(Address::id)
        }
        val criteria2 = join<Employee, Address>() {
            Employee::id.ne(Address::id)
        }
        val criteria3 = criteria1 + criteria2
        val join = Join<Employee, Address>(JoinKind.INNER, Address::class).also {
            val scope = JoinScope(it)
            criteria3(scope)
        }
        assertEquals(
            listOf(
                Criterion.Eq(Employee::id, Address::id),
                Criterion.Ne(Employee::id, Address::id)
            ),
            join.on
        )
    }
}
