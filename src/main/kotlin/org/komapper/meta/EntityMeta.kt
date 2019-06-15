package org.komapper.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class EntityMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val propMetaList: List<PropMeta<T, *>>,
    val tableName: String
) {
    val idList = propMetaList.filter { it.kind is PropKind.Id }
    val version = propMetaList.find { it.kind is PropKind.Version }
    val createdAt = propMetaList.find { it.kind is PropKind.CreatedAt }
    val updatedAt = propMetaList.find { it.kind is PropKind.UpdatedAt }
    val embeddedList = propMetaList
        .flatMap {
            when (it.kind) {
                is PropKind.Embedded -> listOf(it) + it.kind.meta.getEmbeddedPropMetaList()
                else -> emptyList()
            }
        }.reversed()
    val columnNameMap = propMetaList
        .flatMap {
            when (it.kind) {
                is PropKind.Embedded -> it.kind.meta.getLeafPropMetaList()
                else -> listOf(it)
            }
        }.associateBy { it.columnName }
    val propMap = propMetaList
        .flatMap {
            when (it.kind) {
                is PropKind.Embedded -> it.kind.meta.getLeafPropMetaList()
                else -> listOf(it)
            }
        }.associateBy { it.prop }


    fun new(leafs: Map<PropMeta<*, *>, Any?>): T {
        val composites = LinkedHashMap<PropMeta<*, *>, Any?>(leafs)
        for (propMeta in embeddedList) {
            when (propMeta.kind) {
                is PropKind.Embedded -> {
                    val embeddedMeta = propMeta.kind.meta
                    val args = embeddedMeta.propMetaList.map { it.consParam to composites[it] }.toMap()
                    val embedded = embeddedMeta.new(args)
                    composites[propMeta] = embedded
                }
                else -> error("illegal kind: ${propMeta.kind}")
            }
        }
        val args = composites.map { (k, v) -> k.consParam to v }.toMap()
        return cons.callBy(args)
    }

    fun assignId(entity: T, key: String, callNextValue: (String) -> Long): T {
        val idArgs = idList
            .filter { it.kind is PropKind.Id.Sequence }
            .map { it.copyParam to it.next(key, callNextValue) }
            .filter { (_, value) -> value != null }
        if (idArgs.isEmpty()) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val args = mutableMapOf(receiverArg) + idArgs
        return copy(args)
    }

    fun incrementVersion(entity: T): T {
        if (version == null) {
            return entity
        }
        val receiverArg = copy.parameters[0] to entity
        val versionArg = version.copyParam to (version.call(entity).let { version.inc(it) })
        return copy(mapOf(receiverArg, versionArg))
    }

    fun assignTimestamp(entity: T): T {
        if (createdAt == null) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val createdAtArg = createdAt.copyParam to createdAt.now()
        return copy(mapOf(receiverArg, createdAtArg))
    }

    fun updateTimestamp(entity: T): T {
        if (updatedAt == null) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val updatedAtArg = updatedAt.copyParam to updatedAt.now()
        return copy(mapOf(receiverArg, updatedAtArg))
    }

    private fun copy(args: Map<KParameter, Any?>): T {
        return copy.callBy(args)
    }
}
