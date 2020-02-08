package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias Where = WhereScope.() -> Unit

fun where(block: Where): Where = block

infix operator fun (Where).plus(other: Where): Where {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
@Suppress("FunctionName")
class WhereScope(val _add: (Criterion) -> Unit) {

    fun <V> eq(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Eq(prop, value))

    fun <V> ne(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Ne(prop, value))

    fun <V> gt(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Gt(prop, value))

    fun <V> lt(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Lt(prop, value))

    fun <V> ge(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Ge(prop, value))

    fun <V> le(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Le(prop, value))

    fun <V> `in`(prop: KProperty1<*, V>, value: List<V?>) = _add(Criterion.In(prop, value))

    fun <A, B> `in`(
        props: Pair<KProperty1<*, A>, KProperty1<*, B>>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.In2(props, value))

    fun <A, B, C> `in`(
        props: Triple<KProperty1<*, A>, KProperty1<*, B>, KProperty1<*, C>>,
        value: List<Triple<A, B, C>>
    ) = _add(Criterion.In3(props, value))

    fun <V> notIn(prop: KProperty1<*, V>, value: List<V?>) {
        _add(Criterion.NotIn(prop, value))
    }

    fun <A, B> notIn(
        pair: Pair<KProperty1<*, A>, KProperty1<*, B>>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.NotIn2(pair, value))

    fun <A, B, C> notIn(
        triple: Triple<KProperty1<*, A>, KProperty1<*, B>, KProperty1<*, C>>,
        value: List<Triple<A, B, C>>
    ) = _add(Criterion.NotIn3(triple, value))

    fun between(prop: KProperty1<*, *>, begin: Any?, end: Any?) =
        _add(Criterion.Between(prop, begin to end))

    fun like(prop: KProperty1<*, String?>, value: String?) = _add(Criterion.Like(prop, value))

    fun notLike(prop: KProperty1<*, String?>, value: String?) = _add(Criterion.NotLike(prop, value))

    fun not(block: Where) = runBlock(block, Criterion::Not)

    fun and(block: Where) = runBlock(block, Criterion::And)

    fun or(block: Where) = runBlock(block, Criterion::Or)

    private fun runBlock(block: Where, factory: (List<Criterion>) -> Criterion) {
        val criterionList = mutableListOf<Criterion>()
        WhereScope { criterionList.add(it) }.block()
        if (criterionList.isNotEmpty()) {
            _add(factory(criterionList))
        }
    }
}
