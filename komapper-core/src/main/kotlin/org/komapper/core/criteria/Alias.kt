package org.komapper.core.criteria

import kotlin.reflect.KProperty1

class Alias(private val parent: Alias? = null) {
    private var counter = 0
    private val index = if (parent != null) parent.counter++ else counter++
    val name = "t${index}_"
    operator fun <T, R> get(prop: KProperty1<T, R>): AliasProperty<T, R> = AliasProperty(this, prop)
    override fun toString(): String = name
    fun next(): Alias = Alias(this)
}

data class AliasProperty<T, R>(
    val alias: Alias,
    val kProperty1: KProperty1<T, R>
)
