package org.komapper.core.criteria

import org.komapper.core.dsl.Scope

typealias Insert<T> = InsertScope<T>.(Alias) -> Unit

fun <T : Any> insert(block: Insert<T>): Insert<T> = block

infix operator fun <T : Any> (Insert<T>).plus(other: Insert<T>): Insert<T> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

@Scope
class InsertScope<T : Any>(@Suppress("MemberVisibilityCanBePrivate") val _criteria: InsertCriteria<T>) {
    private val valuesScope = ValuesScope(_criteria.alias) { _criteria.values.add(it) }

    fun values(block: Values) = valuesScope.block()
}
