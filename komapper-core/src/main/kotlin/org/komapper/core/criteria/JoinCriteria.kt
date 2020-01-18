package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

typealias JoinCriteria<T, S> = JoinScope<T, S>.() -> Unit

fun <T : Any, S : Any> join(criteria: JoinCriteria<T, S>) = criteria

infix operator fun <T : Any, S : Any> (JoinCriteria<T, S>).plus(other: JoinCriteria<T, S>): JoinCriteria<T, S> {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class JoinScope<T : Any, S : Any>(private val join: Join<T, S>) {

    fun KProperty1<T, *>.eq(value: KProperty1<S, *>) {
        join.on.add(Criterion.Eq(this, value))
    }

    fun KProperty1<T, *>.ne(value: KProperty1<S, *>) {
        join.on.add(Criterion.Ne(this, value))
    }

    fun KProperty1<T, *>.gt(value: KProperty1<S, *>) {
        join.on.add(Criterion.Gt(this, value))
    }

    fun KProperty1<T, *>.ge(value: KProperty1<S, *>) {
        join.on.add(Criterion.Ge(this, value))
    }

    fun KProperty1<T, *>.lt(value: KProperty1<S, *>) {
        join.on.add(Criterion.Lt(this, value))
    }

    fun KProperty1<T, *>.le(value: KProperty1<S, *>) {
        join.on.add(Criterion.Le(this, value))
    }

    fun associate(block: EmptyScope.(T, S) -> Unit) {
        join.association = block
    }
}
