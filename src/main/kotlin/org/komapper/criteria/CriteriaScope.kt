package org.komapper.criteria

interface Terminal {
    operator fun invoke(): Criteria
}

interface OffsetBuilder : Terminal {
    fun offset(value: Int): Terminal
}

interface LimitBuilder : OffsetBuilder {
    fun limit(value: Int): OffsetBuilder
}

interface OrderByBuilder : LimitBuilder {
    fun orderBy(block: OrderByScope.() -> Unit): LimitBuilder
}

interface WhereBuilder : OrderByBuilder {
    fun where(block: WhereScope.() -> Unit): OrderByBuilder
}

class CriteriaScope : WhereBuilder {

    private val whereScope = WhereScope()
    private val orderByScope = OrderByScope()
    private var limit: Int? = null
    private var offset: Int? = null

    override fun where(block: WhereScope.() -> Unit): OrderByBuilder {
        block(whereScope)
        return this
    }

    override fun orderBy(block: OrderByScope.() -> Unit): LimitBuilder {
        block(orderByScope)
        return this
    }

    override fun limit(value: Int): OffsetBuilder {
        limit = value
        return this
    }

    override fun offset(value: Int): Terminal {
        offset = value
        return this
    }

    override operator fun invoke(): Criteria {
        return Criteria(whereScope, orderByScope, limit, offset)
    }
}
