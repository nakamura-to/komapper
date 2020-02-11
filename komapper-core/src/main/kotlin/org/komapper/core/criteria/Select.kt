package org.komapper.core.criteria

import kotlin.reflect.KClass
import org.komapper.core.dsl.Scope

typealias Select<T> = SelectScope<T>.(Alias) -> Unit

fun <T : Any> select(block: Select<T>): Select<T> = block

infix operator fun <T : Any> (Select<T>).plus(other: Select<T>): Select<T> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

data class SelectCriteria<T : Any>(
    val kClass: KClass<T>,
    val alias: Alias = Alias(),
    var distinct: Boolean = false,
    val joins: MutableList<JoinCriteria<Any, Any>> = mutableListOf(),
    val where: MutableList<Criterion> = mutableListOf(),
    val orderBy: MutableList<OrderByItem> = mutableListOf(),
    var limit: Int? = null,
    var offset: Int? = null,
    var forUpdate: ForUpdateCriteria? = null
)

@Scope
class SelectScope<T : Any>(@Suppress("MemberVisibilityCanBePrivate") val _criteria: SelectCriteria<T>) {
    private val whereScope = WhereScope(_criteria.alias) { _criteria.where.add(it) }
    private val orderByScope = OrderByScope(_criteria.alias) { _criteria.orderBy.add(it) }
    private val forUpdateScope = ForUpdateScope { _criteria.forUpdate = it }

    fun distinct(value: Boolean = true) {
        _criteria.distinct = value
    }

    inline fun <reified S : Any> innerJoin(noinline block: Join<T, S>): Alias {
        require(S::class.isData) { "The type parameter S must be a data class." }
        return _join(S::class, JoinKind.INNER, block)
    }

    inline fun <reified S : Any> leftJoin(noinline block: Join<T, S>): Alias {
        require(S::class.isData) { "The type parameter S must be a data class." }
        return _join(S::class, JoinKind.LEFT, block)
    }

    @Suppress("FunctionName")
    fun <S : Any> _join(kClass: KClass<S>, kind: JoinKind, block: Join<T, S>): Alias {
        require(kClass.isData) { "The kClass ${kClass.qualifiedName} must be a data class." }
        val criteria = JoinCriteria<T, S>(kind, kClass, _criteria.alias.next()).also { criteria ->
            val scope =
                JoinScope<T, S>(
                    _criteria.alias,
                    { criteria.on.add(it) },
                    { criteria.association = it })
            scope.block(criteria.alias)
        }
        @Suppress("UNCHECKED_CAST")
        this._criteria.joins.add(criteria as JoinCriteria<Any, Any>)
        return criteria.alias
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
