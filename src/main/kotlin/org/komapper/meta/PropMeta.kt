package org.komapper.meta

import org.komapper.Value
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

data class PropMeta<T, R : Any?>(
    val type: KClass<*>,
    val consParam: KParameter,
    val copyParam: KParameter,
    val prop: KProperty1<T, R>,
    val kind: PropKind<R>,
    val columnName: String
) {

    fun getValues(entity: T): List<Value> {
        val value = prop.call(entity)
        return if (kind is PropKind.Embedded<R>) {
            kind.meta.propMetaList.flatMap { propMeta ->
                if (value == null) {
                    listOf(null to propMeta.type)
                } else {
                    propMeta.getValues(value)
                }
            }
        } else {
            listOf(value to type)
        }
    }

    fun getColumnNames(): List<String> = when (kind) {
        is PropKind.Embedded -> kind.meta.getLeafPropMetaList().flatMap { it.getColumnNames() }
        else -> listOf(columnName)
    }

    fun getLeafPropMetaList(): List<PropMeta<*, *>> = when (kind) {
        is PropKind.Embedded -> kind.meta.getLeafPropMetaList()
        else -> listOf(this)
    }

    fun new(leafValues: Map<PropMeta<*, *>, Any?>): R {
        @Suppress("UNCHECKED_CAST")
        return when (kind) {
            is PropKind.Embedded<R> -> kind.meta.new(leafValues)
            else -> leafValues[this] as R
        }
    }

    fun call(entity: T): R {
        return prop.call(entity)
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
