package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias Where = WhereScope.() -> Unit

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

    fun eq(prop: KProperty1<*, *>, value: Any?) =
        eq(Expression.wrap(prop), value)

    fun eq(prop: Expression.Property, value: Any?) = _add(
        Criterion.Eq(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun ne(prop: KProperty1<*, *>, value: Any?) =
        ne(Expression.wrap(prop), value)

    fun ne(prop: Expression.Property, value: Any?) = _add(
        Criterion.Ne(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun gt(prop: KProperty1<*, *>, value: Any?) =
        gt(Expression.wrap(prop), value)

    fun gt(prop: Expression.Property, value: Any?) = _add(
        Criterion.Gt(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun lt(prop: KProperty1<*, *>, value: Any?) =
        lt(Expression.wrap(prop), value)

    fun lt(prop: Expression.Property, value: Any?) = _add(
        Criterion.Lt(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun ge(prop: KProperty1<*, *>, value: Any?) =
        ge(Expression.wrap(prop), value)

    fun ge(prop: Expression.Property, value: Any?) = _add(
        Criterion.Ge(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun le(prop: KProperty1<*, *>, value: Any?) =
        le(Expression.wrap(prop), value)

    fun le(prop: Expression.Property, value: Any?) = _add(
        Criterion.Le(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun `in`(prop: KProperty1<*, *>, values: List<Any?>) =
        `in`(Expression.wrap(prop), values)

    fun `in`(prop: Expression.Property, values: List<Any?>) =
        _add(Criterion.In(prop, values.map { Expression.wrap(it, prop.kClass) }))

    fun in2(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        values: List<Pair<*, *>>
    ) = in2(Expression.wrap(prop1), Expression.wrap(prop2), values)

    fun in2(
        prop1: Expression.Property,
        prop2: Expression.Property,
        values: List<Pair<*, *>>
    ) = _add(
        Criterion.In2(
            prop1,
            prop2,
            values.map { (a, b) ->
                Expression.wrap(a, prop1.kClass) to Expression.wrap(b, prop2.kClass)
            })
    )

    fun in3(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        prop3: KProperty1<*, *>,
        values: List<Triple<*, *, *>>
    ) = in3(Expression.wrap(prop1), Expression.wrap(prop2), Expression.wrap(prop3), values)

    fun in3(
        prop1: Expression.Property,
        prop2: Expression.Property,
        prop3: Expression.Property,
        values: List<Triple<*, *, *>>
    ) = _add(
        Criterion.In3(
            prop1,
            prop2,
            prop3,
            values.map { (a, b, c) ->
                Triple(
                    Expression.wrap(a, prop1.kClass),
                    Expression.wrap(b, prop2.kClass),
                    Expression.wrap(c, prop3.kClass)
                )
            }
        )
    )

    fun notIn(prop: KProperty1<*, *>, values: List<Any?>) =
        notIn(Expression.wrap(prop), values)

    fun notIn(prop: Expression.Property, values: List<Any?>) =
        _add(Criterion.NotIn(prop, values.map { Expression.wrap(it, prop.kClass) }))

    fun notIn2(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        values: List<Pair<*, *>>
    ) = notIn2(Expression.wrap(prop1), Expression.wrap(prop2), values)

    fun notIn2(
        prop1: Expression.Property,
        prop2: Expression.Property,
        values: List<Pair<*, *>>
    ) = _add(
        Criterion.NotIn2(
            prop1,
            prop2,
            values.map { (a, b) ->
                Expression.wrap(a, prop1.kClass) to Expression.wrap(b, prop2.kClass)
            })
    )

    fun notIn3(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        prop3: KProperty1<*, *>,
        values: List<Triple<*, *, *>>
    ) = notIn3(Expression.wrap(prop1), Expression.wrap(prop2), Expression.wrap(prop3), values)

    fun notIn3(
        prop1: Expression.Property,
        prop2: Expression.Property,
        prop3: Expression.Property,
        values: List<Triple<*, *, *>>
    ) = _add(
        Criterion.NotIn3(
            prop1,
            prop2,
            prop3,
            values.map { (a, b, c) ->
                Triple(
                    Expression.wrap(a, prop1.kClass),
                    Expression.wrap(b, prop2.kClass),
                    Expression.wrap(c, prop3.kClass)
                )
            }
        )
    )

    fun between(prop: KProperty1<*, *>, begin: Any?, end: Any?) =
        between(Expression.wrap(prop), begin, end)

    fun between(prop: Expression.Property, begin: Any?, end: Any?) =
        _add(
            Criterion.Between(
                prop,
                Expression.wrap(begin, prop.kClass) to Expression.wrap(end, prop.kClass)
            )
        )

    fun like(prop: KProperty1<*, *>, value: String?) =
        like(Expression.wrap(prop), value)

    fun like(prop: Expression.Property, value: String?) = _add(
        Criterion.Like(
            prop,
            Expression.wrap(value, prop.kClass)
        )
    )

    fun notLike(prop: KProperty1<*, *>, value: String?) =
        notLike(Expression.wrap(prop), value)

    fun notLike(prop: Expression.Property, value: String?) =
        _add(Criterion.NotLike(prop, Expression.wrap(value, prop.kClass)))

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
