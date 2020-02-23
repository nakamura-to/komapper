package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias Values = ValuesScope.() -> Unit

fun values(block: Values): Values = block

infix operator fun (Values).plus(other: Values): Values {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
@Suppress("FunctionName", "MemberVisibilityCanBePrivate")
class ValuesScope(val _add: (Pair<Expression.Property, Expression>) -> Unit) {
    fun value(prop: KProperty1<*, *>, value: Any?) =
        value(Expression.wrap(prop), value)

    fun value(prop: Expression.Property, value: Any?) =
        _add(prop to Expression.wrap(value, prop.kClass))
}
