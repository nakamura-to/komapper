package org.komapper.core.criteria

import kotlin.reflect.KClass
import org.komapper.core.dsl.Scope

typealias Select<T> = SelectScope<T>.() -> Unit

fun <T : Any> select(block: Select<T>): Select<T> = block

infix operator fun <T : Any> (Select<T>).plus(other: Select<T>): Select<T> {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class SelectScope<T : Any>(@Suppress("MemberVisibilityCanBePrivate") val _criteria: Criteria<T>) {
    private val whereScope = WhereScope { _criteria.where.add(it) }
    private val orderByScope = OrderByScope { _criteria.orderBy.add(it) }
    private val forUpdateScope = ForUpdateScope { _criteria.forUpdate = it }

    inline fun <reified S : Any> innerJoin(noinline block: Join<T, S>) {
        require(S::class.isData) { "The type parameter S must be a data class." }
        _join(S::class, JoinKind.INNER, block)
    }

    inline fun <reified S : Any> leftJoin(noinline block: Join<T, S>) {
        require(S::class.isData) { "The type parameter S must be a data class." }
        _join(S::class, JoinKind.LEFT, block)
    }

    @Suppress("FunctionName")
    fun <S : Any> _join(kClass: KClass<S>, kind: JoinKind, block: Join<T, S>) {
        require(kClass.isData) { "The kClass must be a data class." }
        val criteria = JoinCriteria<T, S>(kind, kClass).also { criteria ->
            val scope =
                JoinScope<T, S>(
                    { criteria.on.add(it) },
                    { criteria.association = it })
            scope.block()
        }
        @Suppress("UNCHECKED_CAST")
        this._criteria.joins.add(criteria as JoinCriteria<Any, Any>)
    }

    fun where(block: Where) = whereScope.block()

    fun orderBy(block: OrderBy) = orderByScope.block()

    fun limit(value: Int) {
        _criteria.limit = value
    }

    fun offset(value: Int) {
        _criteria.offset = value
    }

    fun forUpdate(block: ForUpdate) = forUpdateScope.block()
}
