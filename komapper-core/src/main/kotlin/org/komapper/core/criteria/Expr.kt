package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

sealed class Expr {
    abstract val kClass: KClass<*>

    data class Value(val obj: Any?, override val kClass: KClass<*>) : Expr()

    data class Property<T, R>(val alias: Alias?, val prop: KProperty1<T, R>) : Expr(), KProperty1<T, R> by prop {
        override val kClass = prop.returnType.jvmErasure
    }

    data class Plus(val left: Expr, val right: Expr) : Expr() {
        override val kClass = left.kClass
    }

    data class Minus(val left: Expr, val right: Expr) : Expr() {
        override val kClass = left.kClass
    }

    data class Concat(val left: Expr, val right: Expr) : Expr() {
        override val kClass = left.kClass
    }

    operator fun plus(value: Any?) = Plus(this, wrap(value, this.kClass))
    operator fun minus(value: Any?) = Minus(this, wrap(value, this.kClass))
    infix fun `||`(value: Any?) = Concat(this, wrap(value, this.kClass))

    companion object {
        fun wrap(prop: KProperty1<*, *>): Property<*, *> = when (prop) {
            is Property -> prop
            else -> Property(null, prop)
        }

        fun wrap(expr: Any?, kClass: KClass<*>): Expr = when (expr) {
            is Expr -> expr
            is KProperty1<*, *> -> Property(null, expr)
            else -> Value(expr, kClass)
        }

        fun wrap(value: Any): Expr = wrap(value, value::class)
    }
}

operator fun KProperty1<*, *>.plus(value: Any?) =
    Expr.Plus(
        Expr.wrap(this),
        Expr.wrap(value, this.returnType.jvmErasure)
    )

operator fun KProperty1<*, *>.minus(value: Any?) =
    Expr.Minus(
        Expr.wrap(this),
        Expr.wrap(value, this.returnType.jvmErasure)
    )

infix fun KProperty1<*, *>.`||`(value: Any?) =
    Expr.Concat(
        Expr.wrap(this),
        Expr.wrap(value, this.returnType.jvmErasure)
    )

infix fun String?.`||`(value: Any?) =
    Expr.Concat(
        Expr.wrap(this, String::class),
        Expr.wrap(value, String::class)
    )
