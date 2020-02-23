package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias OrderBy = OrderByScope.() -> Unit

infix operator fun (OrderBy).plus(other: OrderBy): OrderBy {
    val self = this
    return {
        self()
        other()
    }
}

data class OrderByItem(val prop: Expression.Property, val sort: String)

@Scope
class OrderByScope(val _add: (OrderByItem) -> Unit) {

    fun desc(prop: KProperty1<*, *>) = desc(Expression.wrap(prop))

    fun desc(prop: Expression.Property) = _add(OrderByItem(prop, "desc"))

    fun asc(prop: KProperty1<*, *>) = asc(Expression.wrap(prop))

    fun asc(prop: Expression.Property) = _add(OrderByItem(prop, "asc"))
}
