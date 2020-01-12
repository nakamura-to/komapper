package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

@Scope
class OrderByScope(private val items: MutableList<Pair<KProperty1<*, *>, String>>) {

    fun <V> KProperty1<*, V>.desc() {
        items.add(this to "desc")
    }

    fun <V> KProperty1<*, V>.asc() {
        items.add(this to "asc")
    }
}
