package org.komapper.core.criteria

import org.komapper.core.dsl.Scope

@Scope
class SelectScope<T : Any>(val _criteria: MutableCriteria<T>) {
    private val where = WhereScope(_criteria.where)
    private val orderBy = OrderByScope(_criteria.orderBy)

    inline fun <reified S : Any> innerJoin(on: OnScope<T, S>.() -> Unit) {
        require(S::class.isData) { "The S must be a data class." }
        val criteria = mutableListOf<Criterion>()
        val association = Association<T, S>()
        OnScope(criteria, association).on()
        val join = Join(JoinKind.INNER, S::class, criteria, association.block)
        @Suppress("UNCHECKED_CAST")
        _criteria.joins.add(join as Join<Any, Any>)
    }

    inline fun <reified S : Any> leftJoin(on: OnScope<T, S>.() -> Unit) {
        require(S::class.isData) { "The S must be a data class." }
        val criteria = mutableListOf<Criterion>()
        val association = Association<T, S>()
        OnScope(criteria, association).on()
        val join = Join(JoinKind.LEFT, S::class, criteria, association.block)
        @Suppress("UNCHECKED_CAST")
        _criteria.joins.add(join as Join<Any, Any>)
    }

    fun where(block: WhereScope.() -> Unit) = where.block()

    fun orderBy(block: OrderByScope.() -> Unit) = orderBy.block()

    fun limit(value: Int) {
        _criteria.limit = value
    }

    fun offset(value: Int) {
        _criteria.offset = value
    }

    fun forUpdate(block: ForUpdateScope.() -> Unit) {
        ForUpdateScope(_criteria).block()
    }
}
