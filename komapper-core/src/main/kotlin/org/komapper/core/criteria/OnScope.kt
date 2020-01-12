package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

@Scope
@Suppress("FunctionName")
class OnScope<T : Any, S : Any>(private val criteria: MutableList<Criterion>) {

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

    fun not(block: OnScope<T, S>.() -> Unit) = runBlock(block, Criterion::Not)

    fun and(block: OnScope<T, S>.() -> Unit) = runBlock(block, Criterion::And)

    fun or(block: OnScope<T, S>.() -> Unit) = runBlock(block, Criterion::Or)

    private fun runBlock(block: OnScope<T, S>.() -> Unit, context: (List<Criterion>) -> Criterion) {
        val subCriteria = mutableListOf<Criterion>().also {
            OnScope<T, S>(it).block()
        }
        if (subCriteria.isNotEmpty()) {
            criteria.add(context(subCriteria))
        }
    }
}
