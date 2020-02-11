package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UpdateTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun where() {
        val criteria = UpdateCriteria(Address::class)
        val scope = UpdateScope(criteria)
        val query = update<Address> { a ->
            set {
                value(a[Address::street], "aaa")
            }
            where {
                eq(a[Address::id], 1)
            }
        }
        scope.query(criteria.alias)
        assertEquals(1, criteria.set.size)
        assertEquals(1, criteria.where.size)
        assertEquals(criteria.where[0], Criterion.Eq(criteria.alias[Address::id], 1))
    }
}
