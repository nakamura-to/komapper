package org.komapper.core.desc

import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.metadata.IdMeta
import org.komapper.core.metadata.Metadata
import org.komapper.core.metadata.SequenceGenerator

interface PropDescFactory {
    fun <T : Any, R : Any?> create(
        metadata: Metadata<T>?,
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

    override fun <T : Any, R : Any?> create(
        metadata: Metadata<T>?,
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
        val kind = determineKind(metadata, type, consParam, prop, hierarchy, deepGetter) as PropKind<R>
        val column = metadata?.columnList?.find { it.propName == prop.name }
        val name = column?.name ?: namingStrategy.fromKotlinToDb(consParam.name!!)
        val columnLabel = name.split('.').first().toLowerCase()
        val columnName = if (column?.quote == true) quote(name) else name
        return PropDesc(type, consParam, copyParam, prop, deepGetter, kind, columnLabel, columnName)
    }

    protected open fun <T : Any> determineKind(
        metadata: Metadata<T>?,
        type: KClass<*>,
        consParam: KParameter,
        prop: KProperty1<T, *>,
        hierarchy: List<KClass<*>>,
        deepGetter: (Any) -> Any?
    ): PropKind<*> {
        val id = metadata?.idList?.find { it.propName == prop.name }
        val version = metadata?.version?.let { if (it.propName == prop.name) it else null }
        val createdAt = metadata?.createdAt?.let { if (it.propName == prop.name) it else null }
        val updatedAt = metadata?.updatedAt?.let { if (it.propName == prop.name) it else null }
        val embedded = metadata?.embeddedList?.find { it.propName == prop.name }
        return when {
            id != null -> idKind(type, consParam, prop, id)
            version != null -> versionKind(type, consParam, prop)
            createdAt != null -> createdAtKind(type, consParam, prop)
            updatedAt != null -> updatedAtKind(type, consParam, prop)
            embedded != null -> embeddedKind(type, consParam, prop, hierarchy, deepGetter)
            else -> PropKind.Basic
        }
    }

    protected open fun idKind(type: KClass<*>, consParam: KParameter, prop: KProperty1<*, *>, idMeta: IdMeta): PropKind<*> {
        return when (idMeta) {
            is IdMeta.PlainId -> PropKind.Id.Assign
            is IdMeta.SequenceId -> sequenceKind(type, consParam, prop, idMeta.generator)
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
