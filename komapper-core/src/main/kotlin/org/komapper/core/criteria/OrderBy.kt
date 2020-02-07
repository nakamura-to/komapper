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
class OrderByScope(val _add: (Pair<KProperty1<*, *>, String>) -> Unit) {

    fun desc(kProperty1: KProperty1<*, *>) {
        _add(kProperty1 to "desc")
    }

    fun asc(kProperty1: KProperty1<*, *>) {
        _add(kProperty1 to "asc")
    }
}
