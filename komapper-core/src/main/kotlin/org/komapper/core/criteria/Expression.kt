package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

fun expression(block: ExpressionScope.() -> Expression): Expression {
    val scope = ExpressionScope()
    return scope.block()
}

sealed class Expression {
    abstract val kClass: KClass<*>

    data class Value(val obj: Any?, override val kClass: KClass<*>) : Expression()

    data class Property<T, R>(val alias: Alias?, val prop: KProperty1<T, R>) : Expression(), KProperty1<T, R> by prop {
        override val kClass = prop.returnType.jvmErasure
    }

    data class Plus(val left: Expression, val right: Expression) : Expression() {
        override val kClass = left.kClass
    }

    data class Minus(val left: Expression, val right: Expression) : Expression() {
        override val kClass = left.kClass
    }

    data class Concat(val left: Expression, val right: Expression) : Expression() {
        override val kClass = left.kClass
    }

    companion object {
        fun wrap(prop: KProperty1<*, *>): Property<*, *> = when (prop) {
            is Property -> prop
            else -> Property(null, prop)
        }

        fun wrap(expr: Any?, kClass: KClass<*>): Expression = when (expr) {
            is Expression -> expr
            is KProperty1<*, *> -> Property(null, expr)
            else -> Value(expr, kClass)
        }

        fun wrap(value: Any): Expression = wrap(value, value::class)
    }
}

class ExpressionScope {

    operator fun Expression.plus(value: Any?) =
        Expression.Plus(this, Expression.wrap(value, this.kClass))

    operator fun KProperty1<*, *>.plus(value: Any?) =
        Expression.Plus(
            Expression.wrap(this),
            Expression.wrap(value, this.returnType.jvmErasure)
        )

    operator fun Expression.minus(value: Any?) =
        Expression.Minus(this, Expression.wrap(value, this.kClass))

    operator fun KProperty1<*, *>.minus(value: Any?) =
        Expression.Minus(
            Expression.wrap(this),
            Expression.wrap(value, this.returnType.jvmErasure)
        )

    infix fun Expression.`||`(value: Any?) =
        Expression.Concat(this, Expression.wrap(value, this.kClass))

    infix fun KProperty1<*, *>.`||`(value: Any?) =
        Expression.Concat(
            Expression.wrap(this),
            Expression.wrap(value, this.returnType.jvmErasure)
        )

    infix fun String?.`||`(value: Any?) =
        Expression.Concat(
            Expression.wrap(this, String::class),
            Expression.wrap(value, String::class)
        )
}
