package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias Set = SetScope.() -> Unit

infix operator fun (Set).plus(other: Set): Set {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
@Suppress("FunctionName", "MemberVisibilityCanBePrivate")
class SetScope(val _add: (Pair<Expression.Property, Expression>) -> Unit) {
    fun value(prop: KProperty1<*, *>, value: Any?) =
        value(Expression.wrap(prop), value)

    fun value(prop: Expression.Property, value: Any?) =
        _add(prop to Expression.wrap(value, prop.kClass))
}
