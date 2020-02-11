package org.komapper.core.criteria

import kotlin.reflect.KClass
import org.komapper.core.dsl.Scope

typealias Delete<T> = DeleteScope<T>.(Alias) -> Unit

fun <T : Any> delete(block: Delete<T>): Delete<T> = block

infix operator fun <T : Any> (Delete<T>).plus(other: Delete<T>): Delete<T> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

data class DeleteCriteria<T : Any>(
    val kClass: KClass<T>,
    val alias: Alias = Alias(),
    val where: MutableList<Criterion> = mutableListOf()
)

@Scope
class DeleteScope<T : Any>(@Suppress("MemberVisibilityCanBePrivate") val _criteria: DeleteCriteria<T>) {
    private val whereScope = WhereScope(_criteria.alias) { _criteria.where.add(it) }

    fun where(block: Where) = whereScope.block()
}
