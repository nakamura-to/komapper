package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
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
class ValuesScope(val _add: (Pair<Expr.Property<*, *>, Expr>) -> Unit) {
    fun value(prop: KProperty1<*, *>, value: Any?) =
        _add(Expr.wrap(prop) to Expr.wrap(value, prop.returnType.jvmErasure))
}
