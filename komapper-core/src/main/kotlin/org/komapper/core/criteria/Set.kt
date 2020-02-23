package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.dsl.Scope

typealias Set = SetScope.() -> Unit

fun set(block: Set): Set = block

infix operator fun (Set).plus(other: Set): Set {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
@Suppress("FunctionName", "MemberVisibilityCanBePrivate")
class SetScope(val _add: (Pair<Expression.Property<*, *>, Expression>) -> Unit) {
    fun value(prop: KProperty1<*, *>, value: Any?) =
        _add(Expression.wrap(prop) to Expression.wrap(value, prop.returnType.jvmErasure))
}
