package org.komapper.core.criteria

import org.komapper.core.dsl.Scope

typealias SelectCriteria<T> = SelectScope<T>.() -> Unit

fun <T : Any> select(selectCriteria: SelectCriteria<T>): SelectCriteria<T> = selectCriteria

infix operator fun <T : Any> (SelectCriteria<T>).plus(other: SelectCriteria<T>): SelectCriteria<T> {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class SelectScope<T : Any>(val _criteria: Criteria<T>) {
    private val whereScope = WhereScope { _criteria.where.add(it) }
    private val orderByScope = OrderByScope { _criteria.orderBy.add(it) }
    private val forUpdateScope = ForUpdateScope { _criteria.forUpdate = it }

    inline fun <reified S : Any> innerJoin(criteria: JoinCriteria<T, S>) {
        require(S::class.isData) { "The S must be a data class." }
        val join = Join<T, S>(JoinKind.INNER, S::class).also {
            JoinScope(it).criteria()
        }
        @Suppress("UNCHECKED_CAST")
        _criteria.joins.add(join as Join<Any, Any>)
    }

    inline fun <reified S : Any> leftJoin(criteria: JoinCriteria<T, S>) {
        require(S::class.isData) { "The S must be a data class." }
        val join = Join<T, S>(JoinKind.LEFT, S::class).also {
            JoinScope(it).criteria()
        }
        @Suppress("UNCHECKED_CAST")
        _criteria.joins.add(join as Join<Any, Any>)
    }

    fun where(criteria: WhereCriteria) = whereScope.criteria()

    fun orderBy(criteria: OrderByCriteria) = orderByScope.criteria()

    fun limit(value: Int) {
        _criteria.limit = value
    }

    fun offset(value: Int) {
        _criteria.offset = value
    }

    fun forUpdate(criteria: ForUpdateCriteria) = forUpdateScope.criteria()
}
