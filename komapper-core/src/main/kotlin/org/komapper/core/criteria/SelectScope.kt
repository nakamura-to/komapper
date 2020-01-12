package org.komapper.core.criteria

@CriteriaMarker
class SelectScope<T : Any>(val _criteria: MutableCriteria<T>) {
    private val where = WhereScope(_criteria.where)
    private val orderBy = OrderByScope(_criteria.orderBy)

    inline fun <reified S : Any> innerJoin(
        on: OnScope<T, S>.() -> Unit,
        noinline block: (T, S) -> Unit = { _, _ -> }
    ) {
        require(S::class.isData) { "The S must be a data class." }
        val onCriteria = mutableListOf<Criterion>().also { OnScope<T, S>(it).on() }
        @Suppress("UNCHECKED_CAST")
        val join = Join(JoinKind.INNER, S::class, onCriteria, block as (Any, Any) -> Unit)
        _criteria.joins.add(join)
    }

    inline fun <reified S : Any> leftJoin(
        on: OnScope<T, S>.() -> Unit,
        noinline block: (T, S) -> Unit = { _, _ -> }
    ) {
        require(S::class.isData) { "The S must be a data class." }
        val onCriteria = mutableListOf<Criterion>().also { OnScope<T, S>(it).on() }
        @Suppress("UNCHECKED_CAST")
        val join = Join(JoinKind.LEFT, S::class, onCriteria, block as (Any, Any) -> Unit)
        _criteria.joins.add(join)
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

    fun merge(other: Criteria<*>) {
        _criteria.joins.addAll(other.joins)
        _criteria.where.addAll(other.where)
        _criteria.orderBy.addAll(other.orderBy)
        _criteria.limit = other.limit
        _criteria.offset = other.offset
        _criteria.forUpdate = other.forUpdate
    }
}
