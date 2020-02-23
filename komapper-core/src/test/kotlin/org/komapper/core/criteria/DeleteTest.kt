package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DeleteTest {

    private data class Address(
        val id: Int,
        val street: String
    )

    @Test
    fun where() {
        val criteria = DeleteCriteria(Address::class)
        val scope = DeleteScope(criteria)
        val delete = delete<Address> { a ->
            where {
                eq(a[Address::id], 1)
            }
        }
        scope.delete(criteria.alias)
        assertEquals(1, criteria.where.size)
        assertEquals(Criterion.Eq(criteria.alias[Address::id], Expression.wrap(1)), criteria.where[0])
    }
}
