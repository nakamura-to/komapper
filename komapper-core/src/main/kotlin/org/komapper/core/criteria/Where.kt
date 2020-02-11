package org.komapper.core.criteria

import kotlin.reflect.KClass
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

    fun eq(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Eq(AliasProperty(_alias, prop), value))

    fun eq(prop: AliasProperty<*, *>, value: Any?) = _add(Criterion.Eq(prop, value))

    fun ne(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Ne(AliasProperty(_alias, prop), value))

    fun ne(prop: AliasProperty<*, *>, value: Any?) = _add(Criterion.Ne(prop, value))

    fun gt(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Gt(AliasProperty(_alias, prop), value))

    fun gt(prop: AliasProperty<*, *>, value: Any?) = _add(Criterion.Gt(prop, value))

    fun lt(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Lt(AliasProperty(_alias, prop), value))

    fun lt(prop: AliasProperty<*, *>, value: Any?) = _add(Criterion.Lt(prop, value))

    fun ge(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Ge(AliasProperty(_alias, prop), value))

    fun ge(prop: AliasProperty<*, *>, value: Any?) = _add(Criterion.Ge(prop, value))

    fun le(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Le(AliasProperty(_alias, prop), value))

    fun le(prop: AliasProperty<*, *>, value: Any?) = _add(Criterion.Le(prop, value))

    fun `in`(prop: KProperty1<*, *>, value: List<Any?>) = _add(Criterion.In(AliasProperty(_alias, prop), value))

    fun `in`(prop: AliasProperty<*, *>, value: List<Any?>) = _add(Criterion.In(prop, value))

    fun in2(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        value: List<Pair<*, *>>
    ) = _add(Criterion.In2(AliasProperty(_alias, prop1), AliasProperty(_alias, prop2), value))

    fun in2(
        prop1: AliasProperty<*, *>,
        prop2: AliasProperty<*, *>,
        value: List<Pair<*, *>>
    ) = _add(Criterion.In2(prop1, prop2, value))

    fun in3(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        prop3: KProperty1<*, *>,
        value: List<Triple<*, *, *>>
    ) = _add(
        Criterion.In3(
            AliasProperty(_alias, prop1),
            AliasProperty(_alias, prop2),
            AliasProperty(_alias, prop3),
            value
        )
    )

    fun in3(
        prop1: AliasProperty<*, *>,
        prop2: AliasProperty<*, *>,
        prop3: AliasProperty<*, *>,
        value: List<Triple<*, *, *>>
    ) = _add(Criterion.In3(prop1, prop2, prop3, value))

    fun notIn(prop: KProperty1<*, *>, value: List<Any?>) =
        _add(Criterion.NotIn(AliasProperty(_alias, prop), value))

    fun notIn(prop: AliasProperty<*, *>, value: List<Any?>) =
        _add(Criterion.NotIn(prop, value))

    fun notIn2(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        value: List<Pair<*, *>>
    ) = _add(Criterion.NotIn2(AliasProperty(_alias, prop1), AliasProperty(_alias, prop2), value))

    fun notIn2(
        prop1: AliasProperty<*, *>,
        prop2: AliasProperty<*, *>,
        value: List<Pair<*, *>>
    ) = _add(Criterion.NotIn2(prop1, prop2, value))

    fun notIn3(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        prop3: KProperty1<*, *>,
        value: List<Triple<*, *, *>>
    ) = _add(
        Criterion.NotIn3(
            AliasProperty(_alias, prop1),
            AliasProperty(_alias, prop2),
            AliasProperty(_alias, prop3),
            value
        )
    )

    fun notIn3(
        prop1: AliasProperty<*, *>,
        prop2: AliasProperty<*, *>,
        prop3: AliasProperty<*, *>,
        value: List<Triple<*, *, *>>
    ) = _add(Criterion.NotIn3(prop1, prop2, prop3, value))

    fun between(prop: KProperty1<*, *>, begin: Any?, end: Any?) =
        _add(Criterion.Between(AliasProperty(_alias, prop), begin to end))

    fun between(prop: AliasProperty<*, *>, begin: Any?, end: Any?) =
        _add(Criterion.Between(prop, begin to end))

    fun like(prop: KProperty1<*, *>, value: String?) = _add(Criterion.Like(AliasProperty(_alias, prop), value))

    fun like(prop: AliasProperty<*, *>, value: String?) = _add(Criterion.Like(prop, value))

    fun notLike(prop: KProperty1<*, *>, value: String?) =
        _add(Criterion.NotLike(AliasProperty(_alias, prop), value))

    fun notLike(prop: AliasProperty<*, *>, value: String?) = _add(Criterion.NotLike(prop, value))

    inline fun <reified T : Any> exists(noinline block: Select<T>) {
        require(T::class.isData) { "The type parameter T must be a data class." }
        _exists(T::class, block, Criterion::Exists)
    }

    inline fun <reified T : Any> notExists(noinline block: Select<T>) {
        require(T::class.isData) { "The type parameter T must be a data class." }
        _exists(T::class, block, Criterion::NotExists)
    }

    fun <T : Any> _exists(kClass: KClass<T>, block: Select<T>, factory: (SelectCriteria<T>) -> Criterion) {
        require(kClass.isData) { "The kClass ${kClass.qualifiedName} must be a data class." }
        val criteria = SelectCriteria(kClass, _alias.next()).also { criteria ->
            val scope = SelectScope(criteria)
            scope.block(criteria.alias)
        }
        _add(factory(criteria))
    }

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
