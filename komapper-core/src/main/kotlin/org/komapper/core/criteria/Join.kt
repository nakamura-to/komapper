package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

typealias Join<T, S> = JoinScope<T, S>.(Alias) -> Unit

infix operator fun <T : Any, S : Any> (Join<T, S>).plus(other: Join<T, S>): Join<T, S> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

data class JoinCriteria<T : Any, S : Any>(
    val kind: JoinKind,
    val kClass: KClass<S>,
    val alias: Alias,
    val on: MutableList<Criterion> = mutableListOf(),
    var association: Association<T, S>? = null
)

enum class JoinKind {
    INNER,
    LEFT
}

sealed class Association<T, S> {
    data class OneToOne<T, S>(val block: EmptyScope.(T, S?) -> Unit) : Association<T, S>()
    data class OneToMany<T, S>(val block: EmptyScope.(T, List<S>) -> Unit) : Association<T, S>()
}

@Suppress("MemberVisibilityCanBePrivate")
@Scope
class JoinScope<T : Any, S : Any>(
    val _add: (Criterion) -> Unit,
    val _associate: (Association<T, S>) -> Unit
) {

    fun eq(prop: KProperty1<*, *>, value: Any) =
        eq(Expression.wrap(prop), value)

    fun eq(prop: Expression.Property, value: Any) =
        _add(Criterion.Eq(prop, Expression.wrap(value, prop.kClass)))

    fun ne(prop: KProperty1<*, *>, value: Any) =
        ne(Expression.wrap(prop), value)

    fun ne(prop: Expression.Property, value: Any) =
        _add(Criterion.Ne(prop, Expression.wrap(value, prop.kClass)))

    fun gt(prop: KProperty1<*, *>, value: Any) =
        gt(Expression.wrap(prop), value)

    fun gt(prop: Expression.Property, value: Any) =
        _add(Criterion.Gt(prop, Expression.wrap(value, prop.kClass)))

    fun ge(prop: KProperty1<*, *>, value: Any) =
        ge(Expression.wrap(prop), value)

    fun ge(prop: Expression.Property, value: Any) =
        _add(Criterion.Ge(prop, Expression.wrap(value, prop.kClass)))

    fun lt(prop: KProperty1<*, *>, value: Any) =
        lt(Expression.wrap(prop), value)

    fun lt(prop: Expression.Property, value: Any) =
        _add(Criterion.Lt(prop, Expression.wrap(value, prop.kClass)))

    fun le(prop: KProperty1<*, *>, value: Any) =
        le(Expression.wrap(prop), value)

    fun le(prop: Expression.Property, value: Any) =
        _add(Criterion.Le(prop, Expression.wrap(value, prop.kClass)))

    fun oneToOne(block: EmptyScope.(T, S?) -> Unit) = _associate(Association.OneToOne(block))

    fun oneToMany(block: EmptyScope.(T, List<S>) -> Unit) = _associate(Association.OneToMany(block))
}
