package org.komapper.core.criteria

import kotlin.reflect.KClass
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

data class InsertCriteria<T : Any>(
    val kClass: KClass<T>,
    val alias: Alias = Alias(),
    val values: MutableList<Pair<Expr.Property<*, *>, Expr>> = mutableListOf()
)

@Scope
class InsertScope<T : Any>(@Suppress("MemberVisibilityCanBePrivate") val _criteria: InsertCriteria<T>) {
    private val valuesScope = ValuesScope() { _criteria.values.add(it) }

    fun values(block: Values) = valuesScope.block()
}
