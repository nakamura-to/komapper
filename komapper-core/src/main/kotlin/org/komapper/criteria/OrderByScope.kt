package org.komapper.criteria

import kotlin.reflect.KProperty1

@CriteriaMarker
class OrderByScope {

    internal val items = ArrayList<Pair<KProperty1<*, *>, String>>()

    fun <V> KProperty1<*, V>.desc() {
        items.add(this to "desc")
    }

    fun <V> KProperty1<*, V>.asc() {
        items.add(this to "asc")
    }
}
