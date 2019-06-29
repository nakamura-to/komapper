package org.komapper.core.criteria

@CriteriaMarker
class CriteriaScope {

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

    fun where(block: WhereScope.() -> Unit) = where.block()

    fun orderBy(block: OrderByScope.() -> Unit) = orderBy.block()

    fun limit(block: LimitScope.() -> Int) {
        limit = LimitScope.block()
    }

    fun offset(block: OffsetScope.() -> Int) {
        offset = OffsetScope.block()
    }

    internal operator fun invoke(): Criteria {
        return Criteria(where, orderBy, limit, offset)
    }
}
