package org.komapper.meta

import org.komapper.Value
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

class PropMeta<T, R : Any?>(
    val type: KClass<*>,
    val consParam: KParameter,
    val copyParam: KParameter,
    val prop: KProperty1<T, R>,
    val kind: PropKind<R>,
    val columnName: String
) {

    fun new(leafValues: Map<PropMeta<*, *>, Any?>): R {
        @Suppress("UNCHECKED_CAST")
        return when (kind) {
            is PropKind.Embedded<R> -> kind.meta.new(leafValues)
            else -> leafValues[this] as R
        }
    }

    fun copy(
        entity: T,
        predicate: (PropMeta<*, *>) -> Boolean,
        block: (PropMeta<*, *>, Any?) -> Any?
    ): Pair<KParameter, Any?>? = when (kind) {
        is PropKind.Embedded -> {
            val embedded = call(entity)
            val newEmbedded = kind.meta.copy(embedded, predicate, block)
            if (newEmbedded == null) null else copyParam to newEmbedded
        }
        else -> {
            if (predicate(this)) {
                val value = call(entity)
                val newValue = block(this, value as Any)
                copyParam to newValue
            } else {
                null
            }
        }
    }

    fun getValues(entity: T, predicate: (PropMeta<*, *>) -> Boolean): List<Value> = when (kind) {
        is PropKind.Embedded -> {
            val embedded = call(entity)
            kind.meta.getValues(embedded, predicate)
        }
        else -> {
            if (predicate(this)) {
                val value = prop.call(entity)
                listOf(value to type)
            } else {
                emptyList()
            }
        }
    }

    private fun call(entity: T): R {
        return prop.call(entity)
    }

    fun getColumnNames(predicate: (PropMeta<*, *>) -> Boolean): List<String> = when (kind) {
        is PropKind.Embedded -> {
            kind.meta.getColumnNames(predicate)
        }
        else -> {
            if (predicate(this)) listOf(columnName) else emptyList()
        }
    }

    fun getLeafPropMetaList(): List<PropMeta<*, *>> = when (kind) {
        is PropKind.Embedded -> kind.meta.getLeafPropMetaList()
        else -> listOf(this)
    }

    fun next(key: String, callNextValue: (String) -> Long): Any? = when (kind) {
        is PropKind.Id.Sequence -> kind.next(key, callNextValue)
        else -> error("illegal invocation: $kind")
    }

    @Suppress("UNCHECKED_CAST")
    fun inc(value: Any?): R = when (kind) {
        is PropKind.Version<R> -> kind.inc(value as R)
        else -> error("illegal invocation: $kind")
    }

    fun now(): R = when (kind) {
        is PropKind.CreatedAt<R> -> kind.now()
        is PropKind.UpdatedAt<R> -> kind.now()
        else -> error("illegal invocation: $kind")
    }
}

sealed class PropKind<T> {
    sealed class Id<T> : PropKind<T>() {
        object Assign : Id<Any?>()
        data class Sequence<T>(
            private val name: String,
            private val incrementBy: Int,
            private val cast: (Long) -> T
        ) :
            Id<T>() {
            private val cache = ConcurrentHashMap<String, IdGenerator>()

            fun next(key: String, callNextValue: (String) -> Long): T {
                val generator = cache.computeIfAbsent(key) {
                    IdGenerator(incrementBy) { callNextValue(name) }
                }
                return generator.next().let(cast)
            }
        }
    }

    data class CreatedAt<T>(val now: () -> T) : PropKind<T>()
    data class UpdatedAt<T>(val now: () -> T) : PropKind<T>()
    data class Version<T>(val inc: (T) -> T) : PropKind<T>()
    data class Embedded<T>(val meta: EmbeddedMeta<T>) : PropKind<T>()
    object Basic : PropKind<Any?>()
}

private class IdGenerator(private val incrementBy: Int, private val callNextValue: () -> Long) {
    private val lock = ReentrantLock()
    private var base = 0L
    private var step = Long.MAX_VALUE

    fun next(): Long {
        return lock.withLock {
            if (step < incrementBy) {
                base + step++
            } else {
                callNextValue().also {
                    base = it
                    step = 1
                }
            }
        }
    }
}
