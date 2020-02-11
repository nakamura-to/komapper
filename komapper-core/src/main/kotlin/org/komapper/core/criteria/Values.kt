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
class ValuesScope(val _alias: Alias, val _add: (Pair<AliasProperty<*, *>, Any?>) -> Unit) {
    fun value(prop: KProperty1<*, *>, value: Any?) = _add(AliasProperty(_alias, prop) to value)
    fun value(prop: AliasProperty<*, *>, value: Any?) = _add(prop to value)
}
