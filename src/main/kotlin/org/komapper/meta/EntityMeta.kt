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
    val columnNameMap = propMetaList.flatMap { it.getLeafPropMetaList() }.associateBy { it.columnName }
    val propMap = propMetaList.flatMap { it.getLeafPropMetaList() }.associateBy { it.prop }

    fun new(leafValues: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leafValues) }.toMap()
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
