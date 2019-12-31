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
    fun create(
        metadata: Metadata<*>?,
        constructorParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<*, *>,
        dataDescFactory: DataDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): PropDesc
}

open class DefaultPropDescFactory(
    private val quote: (String) -> String,
    private val namingStrategy: NamingStrategy
) : PropDescFactory {

    override fun create(
        metadata: Metadata<*>?,
        constructorParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<*, *>,
        dataDescFactory: DataDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): PropDesc {
        val kClass = constructorParam.type.jvmErasure
        val deepGetter: (Any) -> Any? = { entity -> receiverResolver(entity)?.let { prop.call(it) } }
        val kind = determineKind(metadata, kClass, constructorParam, prop, dataDescFactory, hierarchy, deepGetter)
        val column = metadata?.columnList?.find { it.propName == prop.name }
        val name = column?.name ?: namingStrategy.fromKotlinToDb(constructorParam.name!!)
        val columnLabel = name.split('.').first().toLowerCase()
        val columnName = if (column?.quote == true) quote(name) else name
        return PropDesc(kClass, constructorParam, copyParam, prop, deepGetter, kind, columnLabel, columnName)
    }

    protected open fun determineKind(
        metadata: Metadata<*>?,
        kClass: KClass<*>,
        constructorParam: KParameter,
        prop: KProperty1<*, *>,
        dataDescFactory: DataDescFactory,
        hierarchy: List<KClass<*>>,
        deepGetter: (Any) -> Any?
    ): PropKind {
        val id = metadata?.idList?.find { it.propName == prop.name }
        val version = metadata?.version?.let { if (it.propName == prop.name) it else null }
        val createdAt = metadata?.createdAt?.let { if (it.propName == prop.name) it else null }
        val updatedAt = metadata?.updatedAt?.let { if (it.propName == prop.name) it else null }
        val embedded = metadata?.embeddedList?.find { it.propName == prop.name }
        return when {
            id != null -> idKind(kClass, constructorParam, prop, id)
            version != null -> versionKind(kClass, constructorParam, prop)
            createdAt != null -> createdAtKind(kClass, constructorParam, prop)
            updatedAt != null -> updatedAtKind(kClass, constructorParam, prop)
            embedded != null -> embeddedKind(kClass, constructorParam, prop, dataDescFactory, hierarchy, deepGetter)
            else -> PropKind.Basic
        }
    }

    protected open fun idKind(kClass: KClass<*>, constructorParam: KParameter, prop: KProperty1<*, *>, idMeta: IdMeta): PropKind {
        return when (idMeta) {
            is IdMeta.Assign -> PropKind.Id.Assign
            is IdMeta.Sequence -> sequenceKind(kClass, constructorParam, prop, idMeta.generator)
        }
    }

    protected open fun sequenceKind(
        kClass: KClass<*>,
        constructorParam: KParameter,
        prop: KProperty1<*, *>,
        generator: SequenceGenerator
    ): PropKind {
        val quotedName = if (generator.quote) quote(generator.name) else generator.name
        return when (kClass) {
            Int::class -> PropKind.Id.Sequence(quotedName, generator.incrementBy) { it.toInt() }
            Long::class -> PropKind.Id.Sequence(quotedName, generator.incrementBy) { it }
            else -> error("The @SequenceGenerator parameter must be Int or Long.")
        }
    }

    protected open fun versionKind(kClass: KClass<*>, constructorParam: KParameter, prop: KProperty1<*, *>): PropKind =
        when (kClass) {
            Int::class -> PropKind.Version(Int::inc as (Any) -> Any)
            Long::class -> PropKind.Version(Long::inc as (Any) -> Any)
            else -> error("The @Version parameter must be Int or Long.")
        }

    protected open fun createdAtKind(kClass: KClass<*>, constructorParam: KParameter, prop: KProperty1<*, *>): PropKind =
        when (kClass) {
            LocalDateTime::class -> PropKind.CreatedAt(LocalDateTime::now)
            OffsetDateTime::class -> PropKind.CreatedAt(OffsetDateTime::now)
            else -> error("The @CreatedAt parameter must be either LocalDateTime or OffsetDateTime.")
        }

    protected open fun updatedAtKind(kClass: KClass<*>, constructorParam: KParameter, prop: KProperty1<*, *>): PropKind =
        when (kClass) {
            LocalDateTime::class -> PropKind.UpdatedAt(LocalDateTime::now)
            OffsetDateTime::class -> PropKind.UpdatedAt(OffsetDateTime::now)
            else -> error("The @UpdatedAt parameter must be either LocalDateTime or OffsetDateTime.")
        }

    protected open fun embeddedKind(
        kClass: KClass<*>,
        constructorParam: KParameter,
        prop: KProperty1<*, *>,
        dataDescFactory: DataDescFactory,
        hierarchy: List<KClass<*>>,
        deepGetter: (Any) -> Any?
    ): PropKind =
        when {
            !kClass.isData -> error("The @Embedded parameter must be a data class.")
            kClass.isAbstract -> error("The @Embedded parameter must not be an abstract class.")
            else -> {
                if (kClass in hierarchy) {
                    error(
                        "@Embedded doesn't support circular reference. " +
                                "The type \"${kClass.qualifiedName}\" is circularly referenced in the hierarchy."
                    )
                }
                val dataDesc = dataDescFactory.create(kClass, hierarchy + kClass, deepGetter)
                PropKind.Embedded(dataDesc)
            }
        }
}
