package org.komapper.core.desc

import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.metadata.IdMeta
import org.komapper.core.metadata.Metadata

interface PropDescFactory {
    fun create(
        metadata: Metadata<*>,
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
        metadata: Metadata<*>,
        constructorParam: KParameter,
        copyParam: KParameter,
        prop: KProperty1<*, *>,
        dataDescFactory: DataDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): PropDesc {
        val kClass = prop.returnType.jvmErasure
        val deepGetter: (Any) -> Any? = { entity -> receiverResolver(entity)?.let { prop.call(it) } }
        val id = metadata.idList.find { it.propName == prop.name }
        val version = metadata.version?.let { if (it.propName == prop.name) it else null }
        val createdAt = metadata.createdAt?.let { if (it.propName == prop.name) it else null }
        val updatedAt = metadata.updatedAt?.let { if (it.propName == prop.name) it else null }
        val embedded = metadata.embeddedList.find { it.propName == prop.name }
        val kind = when {
            id != null -> idKind(kClass, prop, id)
            version != null -> versionKind(kClass, prop)
            createdAt != null -> createdAtKind(kClass, prop)
            updatedAt != null -> updatedAtKind(kClass, prop)
            embedded != null -> embeddedKind(kClass, prop, dataDescFactory, hierarchy, deepGetter)
            else -> PropKind.Basic
        }
        val column = metadata.columnList.find { it.propName == prop.name }
        val name = column?.name ?: namingStrategy.fromKotlinToDb(constructorParam.name!!)
        val columnLabel = name.split('.').first().toLowerCase()
        val columnName = if (column?.quote == true) quote(name) else name
        return PropDesc(kClass, constructorParam, copyParam, prop, deepGetter, kind, columnLabel, columnName)
    }

    protected open fun idKind(
        kClass: KClass<*>,
        prop: KProperty1<*, *>,
        idMeta: IdMeta
    ): PropKind {
        return when (idMeta) {
            is IdMeta.Assign -> PropKind.Id.Assign
            is IdMeta.Sequence -> {
                val quotedName = if (idMeta.generator.quote) quote(idMeta.generator.name) else idMeta.generator.name
                when (kClass) {
                    Int::class -> PropKind.Id.Sequence(quotedName, idMeta.generator.incrementBy) { it.toInt() }
                    Long::class -> PropKind.Id.Sequence(quotedName, idMeta.generator.incrementBy) { it }
                    else -> error(
                        "The id property \"${prop.name}\" " +
                                "must be Int or Long."
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun versionKind(kClass: KClass<*>, prop: KProperty1<*, *>): PropKind =
        when (kClass) {
            Int::class -> PropKind.Version(Int::inc as (Any) -> Any)
            Long::class -> PropKind.Version(Long::inc as (Any) -> Any)
            else -> error("The version property \"${prop.name}\" must be Int or Long.")
        }

    protected open fun createdAtKind(
        kClass: KClass<*>,
        prop: KProperty1<*, *>
    ): PropKind =
        when (kClass) {
            LocalDateTime::class -> PropKind.CreatedAt(LocalDateTime::now)
            OffsetDateTime::class -> PropKind.CreatedAt(OffsetDateTime::now)
            else -> error(
                "The createdAt property \"${prop.name}\" " +
                        "must be either LocalDateTime or OffsetDateTime."
            )
        }

    protected open fun updatedAtKind(
        kClass: KClass<*>,
        prop: KProperty1<*, *>
    ): PropKind =
        when (kClass) {
            LocalDateTime::class -> PropKind.UpdatedAt(LocalDateTime::now)
            OffsetDateTime::class -> PropKind.UpdatedAt(OffsetDateTime::now)
            else -> error(
                "The updatedAt property \"${prop.name}\" " +
                        "must be either LocalDateTime or OffsetDateTime."
            )
        }

    protected open fun embeddedKind(
        kClass: KClass<*>,
        prop: KProperty1<*, *>,
        dataDescFactory: DataDescFactory,
        hierarchy: List<KClass<*>>,
        deepGetter: (Any) -> Any?
    ): PropKind =
        when {
            !kClass.isData -> error(
                "The embedded property \"${prop.name}\" " +
                        "must be a data class."
            )
            kClass.isAbstract -> error(
                "The embedded property \"${prop.name}\" " +
                        "must not be an abstract class."
            )
            else -> {
                if (kClass in hierarchy) {
                    error(
                        "The embedded data class \"${kClass.qualifiedName}\" is circularly referenced."
                    )
                }
                val dataDesc = dataDescFactory.create(
                    kClass,
                    prop.returnType.isMarkedNullable,
                    hierarchy + kClass,
                    deepGetter
                )
                PropKind.Embedded(dataDesc)
            }
        }
}
