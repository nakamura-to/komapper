package org.komapper.core.criteria

import kotlin.reflect.KProperty1

@CriteriaMarker
@Suppress("FunctionName")
class OnScope {

    internal val criterionList = ArrayList<Criterion>()

    infix fun KProperty1<*, *>.eq(value: KProperty1<*, *>) {
        criterionList.add(Criterion.Eq(this, value))
    }

    infix fun KProperty1<*, *>.ne(value: KProperty1<*, *>) {
        criterionList.add(Criterion.Ne(this, value))
    }

    infix fun KProperty1<*, *>.gt(value: KProperty1<*, *>) {
        criterionList.add(Criterion.Gt(this, value))
    }

    infix fun KProperty1<*, *>.ge(value: KProperty1<*, *>) {
        criterionList.add(Criterion.Ge(this, value))
    }

    infix fun KProperty1<*, *>.lt(value: KProperty1<*, *>) {
        criterionList.add(Criterion.Lt(this, value))
    }

    infix fun KProperty1<*, *>.le(value: KProperty1<*, *>) {
        criterionList.add(Criterion.Le(this, value))
    }

    fun not(block: OnScope.() -> Unit) {
        val scope = OnScope().apply(block)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.Not(scope.criterionList))
        }
    }

    fun and(block: OnScope.() -> Unit) {
        val scope = OnScope().apply(block)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.And(scope.criterionList))
        }
    }

    fun or(block: OnScope.() -> Unit) {
        val scope = OnScope().apply(block)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.Or(scope.criterionList))
        }
    }

}
