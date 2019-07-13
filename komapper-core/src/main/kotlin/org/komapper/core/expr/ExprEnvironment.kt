package org.komapper.core.expr

import org.komapper.core.value.Value
import kotlin.reflect.KCallable
import kotlin.reflect.jvm.isAccessible

interface ExprEnvironment {
    val ctx: Map<String, Value>
    val topLevelPropertyExtensions: List<KCallable<Any?>>
    val topLevelFunctionExtensions: List<KCallable<Any?>>
}

open class DefaultExprEnvironment(val escape: (CharSequence) -> CharSequence) : ExprEnvironment {

    override val ctx: Map<String, Value> = emptyMap()

    override val topLevelPropertyExtensions: List<KCallable<Any?>> = listOf(
        CharSequence::lastIndex
    ).onEach { it.isAccessible = true }

    override val topLevelFunctionExtensions: List<KCallable<Any?>> = listOf(
        CharSequence::isBlank,
        CharSequence::isNotBlank,
        CharSequence::isNullOrBlank,
        CharSequence::isEmpty,
        CharSequence::isNotEmpty,
        CharSequence::isNullOrEmpty,
        CharSequence::any,
        CharSequence::none
    ).onEach { it.isAccessible = true }

    open fun CharSequence?.escape(): CharSequence? {
        return this?.let { escape(it) }
    }

    open fun CharSequence?.asPrefix(): CharSequence? {
        return this?.let { "${escape(it)}%" }
    }

    open fun CharSequence?.asInfix(): CharSequence? {
        return this?.let { "%${escape(it)}%" }
    }

    open fun CharSequence?.asSuffix(): CharSequence? {
        return this?.let { "%${escape(it)}" }
    }
}
