package org.komapper.core.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EntityMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val propMetaList: List<PropMeta<T, *>>,
    val tableName: String
) {
    val leafPropMetaList = propMetaList.flatMap { it.getLeafPropMetaList() }
    val idList = leafPropMetaList.filter { it.kind is PropKind.Id }
    val sequenceIdList = idList.filter { it.kind is PropKind.Id.Sequence }
    val nonIdList = leafPropMetaList - idList
    val version = leafPropMetaList.find { it.kind is PropKind.Version }
    val createdAt = leafPropMetaList.find { it.kind is PropKind.CreatedAt }
    val updatedAt = leafPropMetaList.find { it.kind is PropKind.UpdatedAt }
    val columnLabelMap = leafPropMetaList.associateBy { it.columnLabel }
    val propMap = leafPropMetaList.associateBy { it.prop }
    val expander: (String) -> List<String> = { prefix -> leafPropMetaList.map { prefix + it.columnName } }

    fun new(leafValues: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leafValues) }.toMap()
        return cons.callBy(args)
    }

    fun assignId(entity: T, key: String, callNextValue: (String) -> Long): T =
        if (sequenceIdList.isEmpty()) {
            entity
        } else {
            copy(entity, { it in sequenceIdList }) { meta, _ -> meta.next(key, callNextValue) }
        }

    fun incrementVersion(entity: T): T =
        if (version == null) {
            entity
        } else {
            copy(entity, { it == version }) { meta, lazy -> lazy()?.let { meta.inc(it) } }
        }

    fun assignTimestamp(entity: T): T =
        if (createdAt == null) {
            entity
        } else {
            copy(entity, { it == createdAt }) { meta, _ -> meta.now() }
        }

    fun updateTimestamp(entity: T): T =
        if (updatedAt == null) {
            entity
        } else {
            copy(entity, { it == updatedAt }) { meta, _ -> meta.now() }
        }

    private fun copy(
        entity: T,
        predicate: (PropMeta<*, *>) -> Boolean,
        block: (PropMeta<*, *>, () -> Any?) -> Any?
    ): T {
        val receiverArg = copy.parameters[0] to entity
        val valueArgs = propMetaList.mapNotNull { it.copy(entity, predicate, block) }.toMap()
        return copy.callBy(mapOf(receiverArg) + valueArgs)
    }
}
