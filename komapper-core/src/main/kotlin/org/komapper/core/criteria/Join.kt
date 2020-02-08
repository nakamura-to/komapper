package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

typealias Join<T, S> = JoinScope<T, S>.() -> Unit

fun <T : Any, S : Any> join(block: Join<T, S>) = block

infix operator fun <T : Any, S : Any> (Join<T, S>).plus(other: Join<T, S>): Join<T, S> {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class JoinScope<T : Any, S : Any>(
    val _add: (Criterion) -> Unit,
    val _associate: (EmptyScope.(T, S) -> Unit) -> Unit
) {
    fun <V> eq(p1: KProperty1<T, V>, p2: KProperty1<S, V>) = _add(Criterion.Eq(p1, p2))

    fun <V> ne(p1: KProperty1<T, V>, p2: KProperty1<S, V>) = _add(Criterion.Ne(p1, p2))

    fun <V> gt(p1: KProperty1<T, V>, p2: KProperty1<S, V>) = _add(Criterion.Gt(p1, p2))

    fun <V> ge(p1: KProperty1<T, V>, p2: KProperty1<S, V>) = _add(Criterion.Ge(p1, p2))

    fun <V> lt(p1: KProperty1<T, V>, p2: KProperty1<S, V>) = _add(Criterion.Lt(p1, p2))

    fun <V> le(p1: KProperty1<T, V>, p2: KProperty1<S, V>) = _add(Criterion.Le(p1, p2))

    fun associate(block: EmptyScope.(T, S) -> Unit) = _associate(block)
}
