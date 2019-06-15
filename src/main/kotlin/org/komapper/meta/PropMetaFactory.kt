package org.komapper.meta

import org.komapper.*
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

interface PropMetaFactory {
    fun <T> create(consParam: KParameter, copyParam: KParameter, prop: KProperty1<T, *>): PropMeta<T>
}

open class DefaultPropMetaFactory(private val namingStrategy: NamingStrategy) : PropMetaFactory {

    override fun <T> create(consParam: KParameter, copyParam: KParameter, prop: KProperty1<T, *>): PropMeta<T> {
        val type = consParam.type.jvmErasure
        val kind = determineKind(consParam, type)
        val column = consParam.findAnnotation<Column>()
        val columnName = column?.name ?: namingStrategy.fromKotlinToDb(consParam.name!!)
        return PropMeta(type, consParam, copyParam, prop, kind, columnName)
    }

    protected open fun determineKind(consParam: KParameter, type: KClass<*>): PropKind {
        val id = consParam.findAnnotation<Id>()
        val version = consParam.findAnnotation<Version>()
        val createdAt = consParam.findAnnotation<CreatedAt>()
        val updatedAt = consParam.findAnnotation<UpdatedAt>()
        return when {
            id != null -> idKind(consParam, type)
            version != null -> versionKind(consParam, type)
            createdAt != null -> createdAtKind(consParam, type)
            updatedAt != null -> updatedAtKind(consParam, type)
            else -> PropKind.Basic
        }
    }

    protected open fun idKind(consParam: KParameter, type: KClass<*>): PropKind {
        val generator = consParam.findAnnotation<SequenceGenerator>()
        return if (generator != null) {
            sequenceKind(consParam, type, generator)
        } else {
            PropKind.Id.Assign
        }
    }

    protected open fun sequenceKind(
        consParam: KParameter,
        type: KClass<*>,
        generator: SequenceGenerator
    ): PropKind = when (type) {
        Int::class -> PropKind.Id.Sequence(generator.name, generator.incrementBy) { it.toInt() }
        Long::class -> PropKind.Id.Sequence(generator.name, generator.incrementBy) { it }
        else -> error("The @SequenceGenerator parameter must be Int or Long.")
    }

    protected open fun versionKind(consParam: KParameter, type: KClass<*>): PropKind = when (type) {
        Int::class -> PropKind.Version { if (it is Int) it.inc() else it }
        Long::class -> PropKind.Version { if (it is Long) it.inc() else it }
        else -> error("The @Version parameter must be Int or Long.")
    }

    protected open fun createdAtKind(consParam: KParameter, type: KClass<*>): PropKind = when (type) {
        LocalDateTime::class -> PropKind.CreatedAt { LocalDateTime.now() }
        else -> error("The @CreatedAt parameter must be LocalDateTime.")
    }

    protected open fun updatedAtKind(consParam: KParameter, type: KClass<*>): PropKind = when (type) {
        LocalDateTime::class -> PropKind.UpdatedAt { LocalDateTime.now() }
        else -> error("The @UpdatedAt parameter must be LocalDateTime.")
    }
}

