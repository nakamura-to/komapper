package org.komapper.core.desc

import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.Column
import org.komapper.core.CreatedAt
import org.komapper.core.Embedded
import org.komapper.core.Id
import org.komapper.core.SequenceGenerator
import org.komapper.core.UpdatedAt
import org.komapper.core.Version

interface PropDescFactory {
    fun <T, R : Any?> create(
        consParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<T, R>,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): PropDesc<T, R>
}

open class DefaultPropDescFactory(
    private val quote: (String) -> String,
    private val namingStrategy: NamingStrategy,
    private val embeddedDescFactory: EmbeddedDescFactory
) : PropDescFactory {

    override fun <T, R : Any?> create(
        consParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<T, R>,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): PropDesc<T, R> {
        val type = consParam.type.jvmErasure
        @Suppress("UNCHECKED_CAST")
        val deepGetter: (Any) -> Any? = { entity -> receiverResolver(entity)?.let { prop.call(it) } }
        @Suppress("UNCHECKED_CAST")
        val kind = determineKind(type, consParam, prop, hierarchy, deepGetter) as PropKind<R>
        val column = consParam.findAnnotation<Column>()
        val name = column?.name ?: namingStrategy.fromKotlinToDb(consParam.name!!)
        val columnLabel = name.split('.').first().toLowerCase()
        val columnName = if (column?.quote == true) quote(name) else name
        return PropDesc(type, consParam, copyParam, prop, deepGetter, kind, columnLabel, columnName)
    }

    protected open fun determineKind(
        type: KClass<*>,
        consParam: KParameter,
        prop: KProperty1<*, *>,
        hierarchy: List<KClass<*>>,
        deepGetter: (Any) -> Any?
    ): PropKind<*> {
        val id = consParam.findAnnotation<Id>()
        val version = consParam.findAnnotation<Version>()
        val createdAt = consParam.findAnnotation<CreatedAt>()
        val updatedAt = consParam.findAnnotation<UpdatedAt>()
        val embedded = consParam.findAnnotation<Embedded>()
        return when {
            id != null -> idKind(type, consParam, prop)
            version != null -> versionKind(type, consParam, prop)
            createdAt != null -> createdAtKind(type, consParam, prop)
            updatedAt != null -> updatedAtKind(type, consParam, prop)
            embedded != null -> embeddedKind(type, consParam, prop, hierarchy, deepGetter)
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
    ): PropKind<*> {
        val quotedName = if (generator.quote) quote(generator.name) else generator.name
        return when (type) {
            Int::class -> PropKind.Id.Sequence(quotedName, generator.incrementBy) { it.toInt() }
            Long::class -> PropKind.Id.Sequence(quotedName, generator.incrementBy) { it }
            else -> error("The @SequenceGenerator parameter must be Int or Long.")
        }
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
            OffsetDateTime::class -> PropKind.CreatedAt(OffsetDateTime::now)
            else -> error("The @CreatedAt parameter must be either LocalDateTime or OffsetDateTime.")
        }

    protected open fun updatedAtKind(type: KClass<*>, consParam: KParameter, prop: KProperty1<*, *>): PropKind<*> =
        when (type) {
            LocalDateTime::class -> PropKind.UpdatedAt(LocalDateTime::now)
            OffsetDateTime::class -> PropKind.UpdatedAt(OffsetDateTime::now)
            else -> error("The @UpdatedAt parameter must be either LocalDateTime or OffsetDateTime.")
        }

    protected open fun embeddedKind(
        type: KClass<*>,
        consParam: KParameter,
        prop: KProperty1<*, *>,
        hierarchy: List<KClass<*>>,
        deepGetter: (Any) -> Any?
    ): PropKind<*> =
        when {
            !type.isData -> error("The @Embedded parameter must be a data class.")
            type.isAbstract -> error("The @Embedded parameter must not be an abstract class.")
            else -> {
                if (type in hierarchy) {
                    error(
                        "@Embedded doesn't support circular reference. " +
                                "The type \"${type.qualifiedName}\" is circularly referenced in the hierarchy."
                    )
                }
                val meta = embeddedDescFactory.create(type, this, hierarchy, deepGetter)
                PropKind.Embedded(meta)
            }
        }
}
