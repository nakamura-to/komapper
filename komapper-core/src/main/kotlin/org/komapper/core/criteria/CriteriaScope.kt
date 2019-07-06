package org.komapper.core.criteria

import kotlin.reflect.KClass

@CriteriaMarker
class CriteriaScope<T : Any>(private val type: KClass<T>) {

    @Suppress("SetterBackingFieldAssignment")
    var joins = mutableListOf<Join>()
        set(value) {
            field.addAll(value)
        }

    @Suppress("SetterBackingFieldAssignment")
    var where = WhereScope()
        set(value) {
            field.criterionList.addAll(value.criterionList)
        }

    @Suppress("SetterBackingFieldAssignment")
    var orderBy = OrderByScope()
        set(value) {
            field.items.addAll(value.items)
        }

    var limit: Int? = null

    var offset: Int? = null

    var forUpdate: ForUpdateScope? = null

    inline fun <reified S : Any> innerJoin(
        on: OnScope.() -> Unit,
        noinline block: (T, S) -> Unit = { _, _ -> }
    ) {
        require(S::class.isData) { "The S must be a data class." }
        require(!S::class.isAbstract) { "The S must not be abstract." }
        val onScope = OnScope().apply(on)
        @Suppress("UNCHECKED_CAST")
        joins.add(Join(JoinKind.INNER, S::class, onScope, block as (Any, Any) -> Unit))
    }

    inline fun <reified S : Any> leftJoin(
        on: OnScope.() -> Unit,
        noinline block: (T, S) -> Unit = { _, _ -> }
    ) {
        require(S::class.isData) { "The S must be a data class." }
        require(!S::class.isAbstract) { "The S must not be abstract." }
        val onScope = OnScope().apply(on)
        @Suppress("UNCHECKED_CAST")
        joins.add(Join(JoinKind.LEFT, S::class, onScope, block as (Any, Any) -> Unit))
    }

    fun where(block: WhereScope.() -> Unit) = where.block()

    fun orderBy(block: OrderByScope.() -> Unit) = orderBy.block()

    fun limit(block: LimitScope.() -> Int) {
        limit = LimitScope.block()
    }

    fun offset(block: OffsetScope.() -> Int) {
        offset = OffsetScope.block()
    }

    fun forUpdate(block: ForUpdateScope.() -> Unit) {
        val forUpdate = ForUpdateScope()
        forUpdate.block()
        this.forUpdate = forUpdate
    }

    internal operator fun invoke(): Criteria {
        return Criteria(type, joins, where, orderBy, limit, offset, forUpdate)
    }
}

