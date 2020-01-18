package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SelectCriteriaTest {

    private data class Address(
        val aaa: Int,
        val bbb: String,
        val ccc: Int,
        val ddd: String,
        val eee: Int,
        val fff: String,
        val ggg: Int
    )

    @Test
    fun where() {
        val criteria = Criteria(Address::class)
        val scope = SelectScope(criteria)
        scope.where {
            Address::aaa.eq(1)
            Address::bbb.ne("B")
            or {
                Address::ccc.gt(2)
                Address::ddd.ge("D")
            }
            and {
                Address::eee.lt(3)
                Address::fff.le("F")
            }
        }
        assertEquals(4, criteria.where.size)
        assertEquals(criteria.where[0], Criterion.Eq(Address::aaa, 1))
        assertEquals(criteria.where[1], Criterion.Ne(Address::bbb, "B"))
        assertEquals(
            criteria.where[2], Criterion.Or(
                listOf(
                    Criterion.Gt(Address::ccc, 2),
                    Criterion.Ge(Address::ddd, "D")
                )
            )
        )
        assertEquals(
            criteria.where[3], Criterion.And(
                listOf(
                    Criterion.Lt(Address::eee, 3),
                    Criterion.Le(Address::fff, "F")
                )
            )
        )
    }

    @Test
    fun orderBy() {
        val criteria = Criteria(Address::class)
        val scope = SelectScope(criteria)
        scope.orderBy {
            Address::aaa.desc()
            Address::bbb.asc()
        }
        assertEquals(2, criteria.orderBy.size)
        assertEquals(criteria.orderBy[0], Address::aaa to "desc")
        assertEquals(criteria.orderBy[1], Address::bbb to "asc")
    }

    @Test
    fun limit() {
        val criteria = Criteria(Address::class)
        val scope = SelectScope(criteria)
        scope.limit(10)
        assertEquals(10, criteria.limit)
    }

    @Test
    fun offset() {
        val criteria = Criteria(Address::class)
        val scope = SelectScope(criteria)
        scope.offset(100)
        assertEquals(100, criteria.offset)
    }

    @Test
    fun where_orderBy_limit_offset() {
        fun test(block: SelectScope<Address>.() -> Unit): Criteria<*> {
            val criteria = Criteria(Address::class)
            val scope = SelectScope(criteria)
            scope.block()
            return criteria
        }

        val criteria = test {
            where {
                Address::aaa.eq(1)
            }
            orderBy {
                Address::bbb.desc()
            }
            limit(5)
            offset(15)
        }

        assertEquals(1, criteria.where.size)
        assertEquals(criteria.where[0], Criterion.Eq(Address::aaa, 1))
        assertEquals(1, criteria.orderBy.size)
        assertEquals(criteria.orderBy[0], Address::bbb to "desc")
        assertEquals(5, criteria.limit)
        assertEquals(15, criteria.offset)
    }
}
