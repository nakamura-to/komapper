package org.komapper.meta

import org.komapper.Value
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

class SequenceIdContext(annotation: org.komapper.SequenceGenerator, private val callNextValue: (String) -> Long) {
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
        data class Sequence(private val annotation: org.komapper.SequenceGenerator) : Id() {
            private val cache = ConcurrentHashMap<String, SequenceIdContext>()

            fun next(key: String, callNextValue: (String) -> Long): Long {
                val idHolder = cache.computeIfAbsent(key) { SequenceIdContext(annotation, callNextValue) }
                return idHolder.get()
            }
        }
    }

    object CreatedAt : PropKind()
    object UpdatedAt : PropKind()
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

fun <T> makePropMeta(
    consParam: KParameter,
    copyFunParam: KParameter,
    kProperty: KProperty1<T, *>,
    namingStrategy: NamingStrategy
): PropMeta<T> {
    val id = consParam.findAnnotation<org.komapper.Id>()
    val version = consParam.findAnnotation<org.komapper.Version>()
    val createdAt = consParam.findAnnotation<org.komapper.CreatedAt>()
    val updatedAt = consParam.findAnnotation<org.komapper.UpdatedAt>()
    // TODO
    val kind = when {
        id != null -> {
            val generator = consParam.findAnnotation<org.komapper.SequenceGenerator>()
            if (generator != null) {
                PropKind.Id.Sequence(generator)
            } else {
                PropKind.Id.Assign
            }
        }
        version != null -> PropKind.Version
        createdAt != null -> PropKind.CreatedAt
        updatedAt != null -> PropKind.UpdatedAt
        else -> PropKind.Basic
    }
    val column = consParam.findAnnotation<org.komapper.Column>()
    val columnName = column?.name ?: namingStrategy.fromKotlinToDb(consParam.name!!)
    return PropMeta(consParam, copyFunParam, kProperty, kind, columnName)
}
