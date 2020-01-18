package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias OrderByCriteria = OrderByScope.() -> Unit

fun orderBy(criteria: OrderByCriteria): OrderByCriteria = criteria

infix operator fun (OrderByCriteria).plus(other: OrderByCriteria): OrderByCriteria {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class OrderByScope(private val add: (Pair<KProperty1<*, *>, String>) -> Unit) {

    fun <V> KProperty1<*, V>.desc() {
        add(this to "desc")
    }

    fun <V> KProperty1<*, V>.asc() {
        add(this to "asc")
    }
}
