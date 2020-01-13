package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

@Scope
@Suppress("FunctionName")
class OnScope<T : Any, S : Any>(
    private val criteria: MutableList<Criterion>,
    private val association: Association<T, S>
) {

    fun KProperty1<T, *>.eq(value: KProperty1<S, *>) {
        criteria.add(Criterion.Eq(this, value))
    }

    fun KProperty1<T, *>.ne(value: KProperty1<S, *>) {
        criteria.add(Criterion.Ne(this, value))
    }

    fun KProperty1<T, *>.gt(value: KProperty1<S, *>) {
        criteria.add(Criterion.Gt(this, value))
    }

    fun KProperty1<T, *>.ge(value: KProperty1<S, *>) {
        criteria.add(Criterion.Ge(this, value))
    }

    fun KProperty1<T, *>.lt(value: KProperty1<S, *>) {
        criteria.add(Criterion.Lt(this, value))
    }

    fun KProperty1<T, *>.le(value: KProperty1<S, *>) {
        criteria.add(Criterion.Le(this, value))
    }

    fun associate(block: EmptyScope.(T, S) -> Unit) {
        association.block = block
    }
}
