package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias OrderBy = OrderByScope.() -> Unit

fun orderBy(block: OrderBy): OrderBy = block

infix operator fun (OrderBy).plus(other: OrderBy): OrderBy {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class OrderByScope(val _alias: Alias, val _add: (OrderByItem) -> Unit) {

    fun desc(prop: KProperty1<*, *>) = _add(OrderByItem(AliasProperty(_alias, prop), "desc"))
    fun desc(prop: AliasProperty<*, *>) = _add(OrderByItem(prop, "desc"))

    fun asc(prop: KProperty1<*, *>) = _add(OrderByItem(AliasProperty(_alias, prop), "asc"))
    fun asc(prop: AliasProperty<*, *>) = _add(OrderByItem(prop, "asc"))
}
