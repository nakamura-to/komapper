package koma.meta

import koma.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

class SequenceIdContext(annotation: SequenceGenerator, private val callNextValue: (String) -> Long) {
    private val name = annotation.name
    private val incrementBy = annotation.incrementBy
    private val lock = ReentrantLock()
    private var base = 0L
    private var step = Long.MAX_VALUE

    fun get(): Long {
        return lock.withLock {
            if (step < incrementBy) {
                base + step++
            } else {
                callNextValue(name).also {
                    base = it
                    step = 1
                }
            }
        }
    }
}

sealed class PropKind {
    sealed class Id : PropKind() {
        object Assign : Id()
        data class Sequence(private val annotation: SequenceGenerator) : Id() {
            private val cache = ConcurrentHashMap<String, SequenceIdContext>()

            fun next(key: String, callNextValue: (String) -> Long): Long {
                val idHolder = cache.computeIfAbsent(key) { SequenceIdContext(annotation, callNextValue) }
                return idHolder.get()
            }
        }
    }

    object Version : PropKind()
    object Basic : PropKind()
}

data class PropMeta<T>(
    val consParam: KParameter,
    val copyFunParam: KParameter,
    val prop: KProperty1<T, *>,
    val kind: PropKind,
    val columnName: String
) {
    val type = prop.returnType.jvmErasure

    fun getValue(entity: T): Value {
        return prop.call(entity) to type
    }
}

fun <T> makePropMeta(consParam: KParameter, copyFunParam: KParameter, kProperty: KProperty1<T, *>): PropMeta<T> {
    val id = consParam.findAnnotation<Id>()
    val version = consParam.findAnnotation<Version>()
    val column = consParam.findAnnotation<Column>()
    val kind = when {
        id != null && version != null -> TODO()
        id != null && version == null -> {
            val generator = consParam.findAnnotation<SequenceGenerator>()
            if (generator != null) {
                PropKind.Id.Sequence(generator)
            } else {
                PropKind.Id.Assign
            }
        }
        id == null && version != null -> PropKind.Version
        else -> PropKind.Basic
    }
    val columnName = column?.name ?: consParam.name!!
    return PropMeta(consParam, copyFunParam, kProperty, kind, columnName)
}
