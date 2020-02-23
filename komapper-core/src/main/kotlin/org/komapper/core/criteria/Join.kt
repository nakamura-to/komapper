package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

typealias Join<T, S> = JoinScope<T, S>.(Alias) -> Unit

fun <T : Any, S : Any> join(block: Join<T, S>) = block

infix operator fun <T : Any, S : Any> (Join<T, S>).plus(other: Join<T, S>): Join<T, S> {
    val self = this
    return { alias ->
        self(alias)
        other(alias)
    }
}

data class JoinCriteria<T : Any, S : Any>(
    val kind: JoinKind,
    val kClass: KClass<S>,
    val alias: Alias,
    val on: MutableList<Criterion> = mutableListOf(),
    var association: (EmptyScope.(T, List<S>) -> Unit)? = null
)

enum class JoinKind {
    INNER,
    LEFT
}

@Suppress("MemberVisibilityCanBePrivate")
@Scope
class JoinScope<T : Any, S : Any>(
    val _add: (Criterion) -> Unit,
    val _associate: (EmptyScope.(T, List<S>) -> Unit) -> Unit
) {

    fun eq(prop: KProperty1<*, *>, value: Any) =
        _add(Criterion.Eq(Expression.wrap(prop), Expression.wrap(value, prop.returnType.jvmErasure)))

    fun ne(prop: KProperty1<*, *>, value: Any) =
        _add(Criterion.Ne(Expression.wrap(prop), Expression.wrap(value, prop.returnType.jvmErasure)))

    fun gt(prop: KProperty1<*, *>, value: Any) =
        _add(Criterion.Gt(Expression.wrap(prop), Expression.wrap(value, prop.returnType.jvmErasure)))

    fun ge(prop: KProperty1<*, *>, value: Any) =
        _add(Criterion.Ge(Expression.wrap(prop), Expression.wrap(value, prop.returnType.jvmErasure)))

    fun lt(prop: KProperty1<*, *>, value: Any) =
        _add(Criterion.Lt(Expression.wrap(prop), Expression.wrap(value, prop.returnType.jvmErasure)))

    fun le(prop: KProperty1<*, *>, value: Any) =
        _add(Criterion.Le(Expression.wrap(prop), Expression.wrap(value, prop.returnType.jvmErasure)))

    fun associate(block: EmptyScope.(T, List<S>) -> Unit) = _associate(block)
}
