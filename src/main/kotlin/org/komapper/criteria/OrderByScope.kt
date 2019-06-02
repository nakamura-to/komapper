package org.komapper.criteria

import kotlin.reflect.KProperty1

class OrderByScope<T> {

    internal val items = ArrayList<Pair<KProperty1<*, *>, String>>()

    fun <V> KProperty1<T, V>.desc() {
        items.add(this to "desc")
    }

    fun <V> KProperty1<T, V>.asc() {
        items.add(this to "asc")
    }
}
