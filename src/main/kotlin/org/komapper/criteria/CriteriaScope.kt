package org.komapper.criteria

@CriteriaMarker
class CriteriaScope {

    private val whereScope = WhereScope()
    private val orderByScope = OrderByScope()
    private var limit: Int? = null
    private var offset: Int? = null

    fun where(block: WhereScope.() -> Unit) = whereScope.block()

    fun orderBy(block: OrderByScope.() -> Unit) = orderByScope.block()

    fun limit(block: LimitScope.() -> Int) {
        limit = LimitScope.block()
    }

    fun offset(block: OffsetScope.() -> Int) {
        offset = OffsetScope.block()
    }

    internal operator fun invoke(): Criteria {
        return Criteria(whereScope, orderByScope, limit, offset)
    }
}
