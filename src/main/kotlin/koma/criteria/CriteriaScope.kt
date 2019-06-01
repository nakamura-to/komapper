package koma.criteria

interface Terminal<T> {
    operator fun invoke(): Criteria<T>
}

interface OffsetBuilder<T> : Terminal<T> {
    fun offset(value: Int): Terminal<T>
}

interface LimitBuilder<T> : OffsetBuilder<T> {
    fun limit(value: Int): OffsetBuilder<T>
}

interface OrderByBuilder<T> : LimitBuilder<T> {
    fun orderBy(block: OrderByScope<T>.() -> Unit): LimitBuilder<T>
}

interface WhereBuilder<T> : OrderByBuilder<T> {
    fun where(block: WhereScope<T>.() -> Unit): OrderByBuilder<T>
}

class CriteriaScope<T> : WhereBuilder<T> {

    private val whereScope = WhereScope<T>()
    private val orderByScope = OrderByScope<T>()
    private var limit: Int? = null
    private var offset: Int? = null

    override fun where(block: WhereScope<T>.() -> Unit): OrderByBuilder<T> {
        block(whereScope)
        return this
    }

    override fun orderBy(block: OrderByScope<T>.() -> Unit): LimitBuilder<T> {
        block(orderByScope)
        return this
    }

    override fun limit(value: Int): OffsetBuilder<T> {
        limit = value
        return this
    }

    override fun offset(value: Int): Terminal<T> {
        offset = value
        return this
    }

    override operator fun invoke(): Criteria<T> {
        return Criteria(whereScope, orderByScope, limit, offset)
    }
}
