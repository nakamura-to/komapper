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
@Suppress("FunctionName", "MemberVisibilityCanBePrivate")
class WhereScope(val _alias: Alias, val _add: (Criterion) -> Unit) {

    fun <V> eq(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Eq(AliasProperty(_alias, prop), value))

    fun <V> eq(prop: AliasProperty<*, V>, value: V?) = _add(Criterion.Eq(prop, value))

    fun <V> ne(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Ne(AliasProperty(_alias, prop), value))

    fun <V> ne(prop: AliasProperty<*, V>, value: V?) = _add(Criterion.Ne(prop, value))

    fun <V> gt(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Gt(AliasProperty(_alias, prop), value))

    fun <V> gt(prop: AliasProperty<*, V>, value: V?) = _add(Criterion.Gt(prop, value))

    fun <V> lt(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Lt(AliasProperty(_alias, prop), value))

    fun <V> lt(prop: AliasProperty<*, V>, value: V?) = _add(Criterion.Lt(prop, value))

    fun <V> ge(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Ge(AliasProperty(_alias, prop), value))

    fun <V> ge(prop: AliasProperty<*, V>, value: V?) = _add(Criterion.Ge(prop, value))

    fun <V> le(prop: KProperty1<*, V>, value: V?) = _add(Criterion.Le(AliasProperty(_alias, prop), value))

    fun <V> le(prop: AliasProperty<*, V>, value: V?) = _add(Criterion.Le(prop, value))

    fun <V> `in`(prop: KProperty1<*, V>, value: List<V?>) = _add(Criterion.In(AliasProperty(_alias, prop), value))

    fun <V> `in`(prop: AliasProperty<*, V>, value: List<V?>) = _add(Criterion.In(prop, value))

    fun <A, B> in2(
        prop1: KProperty1<*, A>,
        prop2: KProperty1<*, B>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.In2(AliasProperty(_alias, prop1), AliasProperty(_alias, prop2), value))

    fun <A, B> in2(
        prop1: AliasProperty<*, A>,
        prop2: AliasProperty<*, B>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.In2(prop1, prop2, value))

    fun <A, B, C> in3(
        prop1: KProperty1<*, A>,
        prop2: KProperty1<*, B>,
        prop3: KProperty1<*, C>,
        value: List<Triple<A, B, C>>
    ) = _add(
        Criterion.In3(
            AliasProperty(_alias, prop1),
            AliasProperty(_alias, prop2),
            AliasProperty(_alias, prop3),
            value
        )
    )

    fun <A, B, C> in3(
        prop1: AliasProperty<*, A>,
        prop2: AliasProperty<*, B>,
        prop3: AliasProperty<*, C>,
        value: List<Triple<A, B, C>>
    ) = _add(Criterion.In3(prop1, prop2, prop3, value))

    fun <V> notIn(prop: KProperty1<*, V>, value: List<V?>) =
        _add(Criterion.NotIn(AliasProperty(_alias, prop), value))

    fun <V> notIn(prop: AliasProperty<*, V>, value: List<V?>) =
        _add(Criterion.NotIn(prop, value))

    fun <A, B> notIn2(
        prop1: KProperty1<*, A>,
        prop2: KProperty1<*, B>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.NotIn2(AliasProperty(_alias, prop1), AliasProperty(_alias, prop2), value))

    fun <A, B> notIn2(
        prop1: AliasProperty<*, A>,
        prop2: AliasProperty<*, B>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.NotIn2(prop1, prop2, value))

    fun <A, B, C> notIn3(
        prop1: KProperty1<*, A>,
        prop2: KProperty1<*, B>,
        prop3: KProperty1<*, C>,
        value: List<Triple<A, B, C>>
    ) = _add(
        Criterion.NotIn3(
            AliasProperty(_alias, prop1),
            AliasProperty(_alias, prop2),
            AliasProperty(_alias, prop3),
            value
        )
    )

    fun <A, B, C> notIn3(
        prop1: AliasProperty<*, A>,
        prop2: AliasProperty<*, B>,
        prop3: AliasProperty<*, C>,
        value: List<Triple<A, B, C>>
    ) = _add(Criterion.NotIn3(prop1, prop2, prop3, value))

    fun <V> between(prop: KProperty1<*, V>, begin: V?, end: V?) =
        _add(Criterion.Between(AliasProperty(_alias, prop), begin to end))

    fun <V> between(prop: AliasProperty<*, V>, begin: V?, end: V?) =
        _add(Criterion.Between(prop, begin to end))

    fun like(prop: KProperty1<*, String?>, value: String?) = _add(Criterion.Like(AliasProperty(_alias, prop), value))

    fun like(prop: AliasProperty<*, String?>, value: String?) = _add(Criterion.Like(prop, value))

    fun notLike(prop: KProperty1<*, String?>, value: String?) =
        _add(Criterion.NotLike(AliasProperty(_alias, prop), value))

    fun notLike(prop: AliasProperty<*, String?>, value: String?) = _add(Criterion.NotLike(prop, value))

    fun not(block: Where) = runBlock(block, Criterion::Not)

    fun and(block: Where) = runBlock(block, Criterion::And)

    fun or(block: Where) = runBlock(block, Criterion::Or)

    private fun runBlock(block: Where, factory: (List<Criterion>) -> Criterion) {
        val criterionList = mutableListOf<Criterion>()
        WhereScope(_alias) { criterionList.add(it) }.block()
        if (criterionList.isNotEmpty()) {
            _add(factory(criterionList))
        }
    }
}
