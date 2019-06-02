package org.komapper.criteria

import kotlin.reflect.KProperty1

class WhereScope<T> {

    internal val criterionList = ArrayList<Criterion>()

    infix fun <V> KProperty1<T, V>.eq(value: V?) {
        criterionList.add(Criterion.Eq(this, value))
    }

    infix fun <V> KProperty1<T, V>.ne(value: V?) {
        criterionList.add(Criterion.Ne(this, value))
    }

    infix fun <V> KProperty1<T, V>.gt(value: V?) {
        criterionList.add(Criterion.Gt(this, value))
    }

    infix fun <V> KProperty1<T, V>.ge(value: V?) {
        criterionList.add(Criterion.Ge(this, value))
    }

    infix fun <V> KProperty1<T, V>.lt(value: V?) {
        criterionList.add(Criterion.Lt(this, value))
    }

    infix fun <V> KProperty1<T, V>.le(value: V?) {
        criterionList.add(Criterion.Le(this, value))
    }

    // in operator
    operator fun <V> Iterable<V>.contains(prop: KProperty1<T, V>): Boolean {
        return criterionList.add(Criterion.In(prop, this))
    }

    fun and(block: WhereScope<T>.() -> Unit) {
        val scope = WhereScope<T>()
        block(scope)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.And(scope.criterionList))
        }
    }

    fun or(block: WhereScope<T>.() -> Unit) {
        val scope = WhereScope<T>()
        block(scope)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.Or(scope.criterionList))
        }
    }
}
