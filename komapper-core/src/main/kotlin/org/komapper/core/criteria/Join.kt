package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

typealias Join<T, S> = JoinScope<T, S>.(Alias) -> Unit

fun <T : Any, S : Any> join(block: Join<T, S>) = block

infix operator fun <T : Any, S : Any> (Join<T, S>).plus(other: Join<T, S>): Join<T, S> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

data class JoinCriteria<T : Any, S : Any>(
    val kind: JoinKind,
    val type: KClass<S>,
    val alias: Alias,
    val on: MutableList<Criterion> = mutableListOf(),
    var association: EmptyScope.(T, S) -> Unit = { _, _ -> }
)

enum class JoinKind {
    INNER,
    LEFT
}

@Suppress("MemberVisibilityCanBePrivate")
@Scope
class JoinScope<T : Any, S : Any>(
    val _alias: Alias,
    val _add: (Criterion) -> Unit,
    val _associate: (EmptyScope.(T, S) -> Unit) -> Unit
) {
    fun <V> eq(prop1: KProperty1<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Eq(AliasProperty(_alias, prop1), prop2))

    fun <V> eq(prop1: AliasProperty<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Eq(prop1, prop2))

    fun <V> ne(prop1: KProperty1<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Ne(AliasProperty(_alias, prop1), prop2))

    fun <V> ne(prop1: AliasProperty<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Ne(prop1, prop2))

    fun <V> gt(prop1: KProperty1<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Gt(AliasProperty(_alias, prop1), prop2))

    fun <V> gt(prop1: AliasProperty<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Gt(prop1, prop2))

    fun <V> ge(prop1: KProperty1<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Ge(AliasProperty(_alias, prop1), prop2))

    fun <V> ge(prop1: AliasProperty<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Ge(prop1, prop2))

    fun <V> lt(prop1: KProperty1<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Lt(AliasProperty(_alias, prop1), prop2))

    fun <V> lt(prop1: AliasProperty<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Lt(prop1, prop2))

    fun <V> le(prop1: KProperty1<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Le(AliasProperty(_alias, prop1), prop2))

    fun <V> le(prop1: AliasProperty<T, V>, prop2: AliasProperty<S, V>) =
        _add(Criterion.Le(prop1, prop2))

    fun associate(block: EmptyScope.(T, S) -> Unit) = _associate(block)
}
