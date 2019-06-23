package org.komapper.meta

import org.komapper.core.Value
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EntityMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val propMetaList: List<PropMeta<T, *>>,
    val tableName: String
) {
    private val leafPropMetaList = propMetaList.flatMap { it.getLeafPropMetaList() }
    val idList = leafPropMetaList.filter { it.kind is PropKind.Id }
    val sequenceIdList = leafPropMetaList.filter { it.kind is PropKind.Id.Sequence }
    val version = leafPropMetaList.find { it.kind is PropKind.Version }
    val createdAt = leafPropMetaList.find { it.kind is PropKind.CreatedAt }
    val updatedAt = leafPropMetaList.find { it.kind is PropKind.UpdatedAt }
    val columnNames = leafPropMetaList.map { it.columnName }
    val idColumnNames = idList.map { it.columnName }
    val nonIdColumnNames = (leafPropMetaList - idList).map { it.columnName }
    val columnLabelMap = leafPropMetaList.associateBy { it.columnLabel }
    val propMap = leafPropMetaList.associateBy { it.prop }
    val expander: (String) -> List<String> = { prefix -> columnNames.map { prefix + it } }

    fun new(leaves: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leaves) }.toMap()
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

    fun getValues(entity: T): List<Value> =
        propMetaList.flatMap { it.getValues(entity, { true }) }

    fun getIdValues(entity: T): List<Value> =
        propMetaList.flatMap { meta -> meta.getValues(entity, { it in idList }) }

    fun getNonIdValues(entity: T): List<Value> =
        propMetaList.flatMap { meta -> meta.getValues(entity, { it !in idList }) }

    fun getVersionValue(entity: T): Value =
        propMetaList.flatMap { meta -> meta.getValues(entity, { it == version }) }.first()

}
