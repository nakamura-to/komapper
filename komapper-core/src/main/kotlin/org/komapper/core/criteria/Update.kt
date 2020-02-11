package org.komapper.core.criteria

import kotlin.reflect.KClass
import org.komapper.core.dsl.Scope

typealias Update<T> = UpdateScope<T>.(Alias) -> Unit

fun <T : Any> update(block: Update<T>): Update<T> = block

infix operator fun <T : Any> (Update<T>).plus(other: Update<T>): Update<T> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

data class UpdateCriteria<T : Any>(
    val kClass: KClass<T>,
    val alias: Alias = Alias(),
    val set: MutableList<Pair<AliasProperty<*, *>, Any?>> = mutableListOf(),
    val where: MutableList<Criterion> = mutableListOf()
)

@Scope
class UpdateScope<T : Any>(@Suppress("MemberVisibilityCanBePrivate") val _criteria: UpdateCriteria<T>) {
    private val setScope = SetScope(_criteria.alias) { _criteria.set.add(it) }
    private val whereScope = WhereScope(_criteria.alias) { _criteria.where.add(it) }

    fun set(block: Set) = setScope.block()

    fun where(block: Where) = whereScope.block()
}
