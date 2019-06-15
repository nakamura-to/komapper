package org.komapper.meta

import org.komapper.*
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

interface PropMetaFactory {
    fun <T, R : Any?> create(
        consParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<T, R>,
        hierarchy: List<KClass<*>>
    ): PropMeta<T, R>
}

open class DefaultPropMetaFactory(
    private val namingStrategy: NamingStrategy,
    private val embeddedMetaFactory: EmbeddedMetaFactory
) : PropMetaFactory {

    override fun <T, R : Any?> create(
        consParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<T, R>,
        hierarchy: List<KClass<*>>
    ): PropMeta<T, R> {
        val type = consParam.type.jvmErasure
        @Suppress("UNCHECKED_CAST")
        val kind = determineKind(type, consParam, prop, hierarchy) as PropKind<R>
        val column = consParam.findAnnotation<Column>()
        val columnName = column?.name ?: namingStrategy.fromKotlinToDb(consParam.name!!)
        return PropMeta(type, consParam, copyParam, prop, kind, columnName)
    }

    protected open fun determineKind(
        type: KClass<*>,
        consParam: KParameter,
        prop: KProperty1<*, *>,
        hierarchy: List<KClass<*>>
    ): PropKind<*> {
        val id = consParam.findAnnotation<Id>()
        val version = consParam.findAnnotation<Version>()
        val createdAt = consParam.findAnnotation<CreatedAt>()
        val updatedAt = consParam.findAnnotation<UpdatedAt>()
        val embedded = consParam.findAnnotation<Embedded>()
        if (hierarchy.size > 1) {
            val lazyMessage: (String) -> String =
                { a -> "The embedded class \"${hierarchy.last().qualifiedName}\" must not have the $a parameter." }
            when {
                id != null -> error(lazyMessage("@Id"))
                version != null -> error(lazyMessage("@Version"))
                createdAt != null -> error(lazyMessage("@CreatedAt"))
                updatedAt != null -> error(lazyMessage("@UpdatedAt"))
            }
        }
        return when {
            id != null -> idKind(type, consParam, prop)
            version != null -> versionKind(type, consParam, prop)
            createdAt != null -> createdAtKind(type, consParam, prop)
            updatedAt != null -> updatedAtKind(type, consParam, prop)
            embedded != null -> embeddedKind(type, consParam, prop, hierarchy)
            else -> PropKind.Basic
        }
    }

    protected open fun idKind(type: KClass<*>, consParam: KParameter, prop: KProperty1<*, *>): PropKind<*> {
        val generator = consParam.findAnnotation<SequenceGenerator>()
        return if (generator != null) {
            sequenceKind(type, consParam, prop, generator)
        } else {
            PropKind.Id.Assign
        }
    }

    protected open fun sequenceKind(
        type: KClass<*>,
        consParam: KParameter,
        prop: KProperty1<*, *>,
        generator: SequenceGenerator
    ): PropKind<*> = when (type) {
        Int::class -> PropKind.Id.Sequence(generator.name, generator.incrementBy) { it.toInt() }
        Long::class -> PropKind.Id.Sequence(generator.name, generator.incrementBy) { it }
        else -> error("The @SequenceGenerator parameter must be Int or Long.")
    }

    protected open fun versionKind(type: KClass<*>, consParam: KParameter, prop: KProperty1<*, *>): PropKind<*> =
        when (type) {
            Int::class -> PropKind.Version(Int::inc)
            Long::class -> PropKind.Version(Long::inc)
            else -> error("The @Version parameter must be Int or Long.")
        }

    protected open fun createdAtKind(type: KClass<*>, consParam: KParameter, prop: KProperty1<*, *>): PropKind<*> =
        when (type) {
            LocalDateTime::class -> PropKind.CreatedAt(LocalDateTime::now)
            else -> error("The @CreatedAt parameter must be LocalDateTime.")
        }

    protected open fun updatedAtKind(type: KClass<*>, consParam: KParameter, prop: KProperty1<*, *>): PropKind<*> =
        when (type) {
            LocalDateTime::class -> PropKind.UpdatedAt(LocalDateTime::now)
            else -> error("The @UpdatedAt parameter must be LocalDateTime.")
        }

    protected open fun embeddedKind(
        type: KClass<*>,
        consParam: KParameter,
        prop: KProperty1<*, *>,
        hierarchy: List<KClass<*>>
    ): PropKind<*> =
        when {
            !type.isData -> error("The @Embedded parameter must be a data class.")
            type.isAbstract -> error("The @Embedded parameter must not be an abstract class.")
            else -> {
                if (type in hierarchy) {
                    error(
                        "@Embedded does'n support circular reference. " +
                                "The type \"${type.qualifiedName}\" is circularly referenced in the hierarchy."
                    )
                }
                val meta = embeddedMetaFactory.create(type, this, hierarchy)
                PropKind.Embedded(meta)
            }
        }
}

