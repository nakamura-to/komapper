package org.komapper.core.expr

import kotlin.reflect.KCallable
import kotlin.reflect.jvm.isAccessible

interface ExprExtensions {
    val topLevelPropertyExtensions: List<KCallable<Any?>>
    val topLevelFunctionExtensions: List<KCallable<Any?>>
}

open class DefaultExprExtensions(val escape: (CharSequence) -> CharSequence) : ExprExtensions {

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
