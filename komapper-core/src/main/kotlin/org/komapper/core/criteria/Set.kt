package org.komapper.core.criteria

import kotlin.reflect.KProperty1
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
class SetScope(val _alias: Alias, val _add: (Pair<AliasProperty<*, *>, Any?>) -> Unit) {
    fun value(prop: KProperty1<*, *>, value: Any?) = _add(AliasProperty(_alias, prop) to value)

    fun value(prop: AliasProperty<*, *>, value: Any?) = _add(prop to value)
}
