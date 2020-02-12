package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SelectTest {

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
    fun distinct() {
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        val select = select<Address> {
            distinct()
        }
        scope.select(criteria.alias)
        assertTrue(criteria.distinct)
    }

    @Test
    fun where() {
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        val select = select<Address> {
            where {
                eq(Address::aaa, 1)
                ne(Address::bbb, "B")
                or {
                    gt(Address::ccc, 2)
                    ge(Address::ddd, "D")
                }
                and {
                    lt(Address::eee, 3)
                    le(Address::fff, "F")
                }
            }
        }
        scope.select(criteria.alias)
        assertEquals(4, criteria.where.size)
        assertEquals(criteria.where[0], Criterion.Eq(Expr.wrap(Address::aaa), Expr.wrap(1)))
        assertEquals(criteria.where[1], Criterion.Ne(Expr.wrap(Address::bbb), Expr.wrap("B")))
        assertEquals(
            criteria.where[2], Criterion.Or(
                listOf(
                    Criterion.Gt(Expr.wrap(Address::ccc), Expr.wrap(2)),
                    Criterion.Ge(Expr.wrap(Address::ddd), Expr.wrap("D"))
                )
            )
        )
        assertEquals(
            criteria.where[3], Criterion.And(
                listOf(
                    Criterion.Lt(Expr.wrap(Address::eee), Expr.wrap(3)),
                    Criterion.Le(Expr.wrap(Address::fff), Expr.wrap("F"))
                )
            )
        )
    }

    @Test
    fun orderBy() {
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        val select = select<Address> { a ->
            orderBy {
                desc(Address::aaa)
                asc(a[Address::bbb])
            }
        }
        scope.select(criteria.alias)
        assertEquals(2, criteria.orderBy.size)
        assertEquals(criteria.orderBy[0], OrderByItem(Expr.wrap(Address::aaa), "desc"))
        assertEquals(criteria.orderBy[1], OrderByItem(criteria.alias[Address::bbb], "asc"))
    }

    @Test
    fun limit() {
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        val select = select<Address> {
            limit(10)
        }
        scope.select(criteria.alias)
        assertEquals(10, criteria.limit)
    }

    @Test
    fun offset() {
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        val select = select<Address> {
            offset(100)
        }
        scope.select(criteria.alias)
        assertEquals(100, criteria.offset)
    }

    @Test
    fun where_orderBy_limit_offset() {
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        val select = select<Address> { a ->
            where {
                eq(a[Address::aaa], 1)
            }
            orderBy {
                desc(a[Address::bbb])
            }
            limit(5)
            offset(15)
        }
        scope.select(criteria.alias)
        assertEquals(1, criteria.where.size)
        assertEquals(Criterion.Eq(criteria.alias[Address::aaa], Expr.wrap(1)), criteria.where[0])
        assertEquals(1, criteria.orderBy.size)
        assertEquals(OrderByItem(criteria.alias[Address::bbb], "desc"), criteria.orderBy[0])
        assertEquals(5, criteria.limit)
        assertEquals(15, criteria.offset)
    }

    @Test
    fun plus() {
        val s1 = select<Address> {
            where {
                eq(Address::aaa, 1)
            }
        }
        val s2 = select<Address> {
            orderBy {
                desc(Address::bbb)
            }
            limit(5)
            offset(15)
        }
        val s3 = s1 + s2
        val criteria = SelectCriteria(Address::class)
        val scope = SelectScope(criteria)
        scope.s3(criteria.alias)
        assertEquals(1, criteria.where.size)
        assertEquals(Criterion.Eq(Expr.wrap(Address::aaa), Expr.wrap(1)), criteria.where[0])
        assertEquals(1, criteria.orderBy.size)
        assertEquals(OrderByItem(Expr.wrap(Address::bbb), "desc"), criteria.orderBy[0])
        assertEquals(5, criteria.limit)
        assertEquals(15, criteria.offset)
    }
}
