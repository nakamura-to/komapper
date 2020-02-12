package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
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

    fun eq(prop: KProperty1<*, *>, value: Any?) = _add(
        Criterion.Eq(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun ne(prop: KProperty1<*, *>, value: Any?) = _add(
        Criterion.Ne(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun gt(prop: KProperty1<*, *>, value: Any?) = _add(
        Criterion.Gt(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun lt(prop: KProperty1<*, *>, value: Any?) = _add(
        Criterion.Lt(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun ge(prop: KProperty1<*, *>, value: Any?) = _add(
        Criterion.Ge(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun le(prop: KProperty1<*, *>, value: Any?) = _add(
        Criterion.Le(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun `in`(prop: KProperty1<*, *>, values: List<Any?>) =
        _add(Criterion.In(Expr.wrap(prop), values.map { Expr.wrap(it, prop.returnType.jvmErasure) }))

    fun in2(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        values: List<Pair<*, *>>
    ) = _add(
        Criterion.In2(
            Expr.wrap(prop1),
            Expr.wrap(prop2),
            values.map { (a, b) ->
                Expr.wrap(a, prop1.returnType.jvmErasure) to Expr.wrap(
                    b,
                    prop2.returnType.jvmErasure
                )
            })
    )

    fun in3(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        prop3: KProperty1<*, *>,
        values: List<Triple<*, *, *>>
    ) = _add(
        Criterion.In3(
            Expr.wrap(prop1),
            Expr.wrap(prop2),
            Expr.wrap(prop3),
            values.map { (a, b, c) ->
                Triple(
                    Expr.wrap(a, prop1.returnType.jvmErasure),
                    Expr.wrap(b, prop2.returnType.jvmErasure),
                    Expr.wrap(c, prop3.returnType.jvmErasure)
                )
            }
        )
    )

    fun notIn(prop: KProperty1<*, *>, values: List<Any?>) =
        _add(Criterion.NotIn(Expr.wrap(prop), values.map { Expr.wrap(it, prop.returnType.jvmErasure) }))

    fun notIn2(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        values: List<Pair<*, *>>
    ) = _add(
        Criterion.NotIn2(
            Expr.wrap(prop1),
            Expr.wrap(prop2),
            values.map { (a, b) ->
                Expr.wrap(a, prop1.returnType.jvmErasure) to Expr.wrap(
                    b,
                    prop2.returnType.jvmErasure
                )
            })
    )

    fun notIn3(
        prop1: KProperty1<*, *>,
        prop2: KProperty1<*, *>,
        prop3: KProperty1<*, *>,
        values: List<Triple<*, *, *>>
    ) = _add(
        Criterion.NotIn3(
            Expr.wrap(prop1),
            Expr.wrap(prop2),
            Expr.wrap(prop3),
            values.map { (a, b, c) ->
                Triple(
                    Expr.wrap(a, prop1.returnType.jvmErasure),
                    Expr.wrap(b, prop2.returnType.jvmErasure),
                    Expr.wrap(c, prop3.returnType.jvmErasure)
                )
            }
        )
    )

    fun between(prop: KProperty1<*, *>, begin: Any?, end: Any?) =
        _add(
            Criterion.Between(
                Expr.wrap(prop),
                Expr.wrap(begin, prop.returnType.jvmErasure) to Expr.wrap(end, prop.returnType.jvmErasure)
            )
        )

    fun like(prop: KProperty1<*, *>, value: String?) = _add(
        Criterion.Like(
            Expr.wrap(prop),
            Expr.wrap(value, prop.returnType.jvmErasure)
        )
    )

    fun notLike(prop: KProperty1<*, *>, value: String?) =
        _add(Criterion.NotLike(Expr.wrap(prop), Expr.wrap(value, prop.returnType.jvmErasure)))

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
