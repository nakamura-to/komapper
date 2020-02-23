package org.komapper.core.criteria

import kotlin.reflect.KProperty1

class Alias(parent: Alias? = null) {
    private var counter = 0
    private val index = if (parent != null) parent.counter++ else counter++
    val name = "t${index}_"
    operator fun get(prop: KProperty1<*, *>) = Expression.Property(this, prop)
    override fun toString() = name
    fun next() = Alias(this)
}
