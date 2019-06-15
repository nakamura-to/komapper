package org.komapper.meta

import org.komapper.Value
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

data class PropMeta<T>(
    val type: KClass<*>,
    val consParam: KParameter,
    val copyParam: KParameter,
    val prop: KProperty1<T, *>,
    val kind: PropKind,
    val columnName: String
) {

    fun getValue(entity: T): Value {
        return prop.call(entity) to type
    }

    fun call(entity: T): Any? {
        return prop.call(entity)
    }

    fun next(key: String, callNextValue: (String) -> Long): Any? = when (kind) {
        is PropKind.Id.Sequence -> kind.next(key, callNextValue)
        else -> error("illegal invocation: $kind")
    }

    fun inc(value: Any?): Any? = when (kind) {
        is PropKind.Version -> kind.inc(value)
        else -> error("illegal invocation: $kind")
    }

    fun now(): Any = when (kind) {
        is PropKind.CreatedAt -> kind.now()
        is PropKind.UpdatedAt -> kind.now()
        else -> error("illegal invocation: $kind")
    }
}

sealed class PropKind {
    sealed class Id : PropKind() {
        object Assign : Id()
        data class Sequence(
            private val name: String,
            private val incrementBy: Int,
            private val cast: (Long) -> Any
        ) :
            Id() {
            private val cache = ConcurrentHashMap<String, IdGenerator>()

            fun next(key: String, callNextValue: (String) -> Long): Any {
                val generator = cache.computeIfAbsent(key) {
                    IdGenerator(incrementBy) { callNextValue(name) }
                }
                return generator.next().let(cast)
            }
        }
    }

    data class CreatedAt(val now: () -> Any) : PropKind()
    data class UpdatedAt(val now: () -> Any) : PropKind()
    data class Version(val inc: (Any?) -> Any?) : PropKind()
    object Basic : PropKind()
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
