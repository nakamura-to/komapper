package org.komapper.meta

import org.komapper.Value
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
    val idList = propMetaList.flatMap { it.getLeafPropMetaList() }.filter { it.kind is PropKind.Id }
    val version = propMetaList.flatMap { it.getLeafPropMetaList() }.find { it.kind is PropKind.Version }
    val createdAt = propMetaList.flatMap { it.getLeafPropMetaList() }.find { it.kind is PropKind.CreatedAt }
    val updatedAt = propMetaList.flatMap { it.getLeafPropMetaList() }.find { it.kind is PropKind.UpdatedAt }
    val columnNameMap = propMetaList.flatMap { it.getLeafPropMetaList() }.associateBy { it.columnName }
    val propMap = propMetaList.flatMap { it.getLeafPropMetaList() }.associateBy { it.prop }

    fun new(leafValues: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leafValues) }.toMap()
        return cons.callBy(args)
    }

    fun assignId(entity: T, key: String, callNextValue: (String) -> Long): T {
        if (idList.none { it.kind is PropKind.Id.Sequence }) {
            return entity
        }
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it.kind is PropKind.Id.Sequence }) { sequencePropMeta, _ ->
                sequencePropMeta.next(key, callNextValue)
            }
        }.toMap()
        val receiverArg = copy.parameters[0] to entity
        return copy(mapOf(receiverArg) + valueArgs)
    }

    fun incrementVersion(entity: T): T {
        if (version == null) {
            return entity
        }
        val receiverArg = copy.parameters[0] to entity
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it == version }) { _, currentValue ->
                version.inc(currentValue)
            }
        }.toMap()
        return copy(mapOf(receiverArg) + valueArgs)
    }

    fun assignTimestamp(entity: T): T {
        if (createdAt == null) {
            return entity
        }
        val receiverArg = copy.parameters[0] to entity
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it == createdAt }) { _, _ -> createdAt.now() }
        }.toMap()
        return copy(mapOf(receiverArg) + valueArgs)
    }

    fun updateTimestamp(entity: T): T {
        if (updatedAt == null) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it == updatedAt }) { _, _ -> updatedAt.now() }
        }.toMap()
        return copy(mapOf(receiverArg) + valueArgs)
    }

    private fun copy(args: Map<KParameter, Any?>): T {
        return copy.callBy(args)
    }

    fun getColumnNames(): List<String> {
        return propMetaList.flatMap { it.getColumnNames { true } }
    }

    fun getIdColumnNames(): List<String> {
        return propMetaList.flatMap { meta -> meta.getColumnNames { it in idList } }
    }

    fun getNonIdColumnNames(): List<String> {
        return propMetaList.flatMap { meta -> meta.getColumnNames { it !in idList } }
    }

    fun getValues(entity: T): List<Value> {
        return propMetaList.flatMap { it.getValues(entity, { true }) }
    }

    fun getIdValues(entity: T): List<Value> {
        return propMetaList.flatMap { meta -> meta.getValues(entity, { it in idList }) }
    }

    fun getNonIdValues(entity: T): List<Value> {
        return propMetaList.flatMap { meta -> meta.getValues(entity, { it !in idList }) }
    }

    fun getVersionValue(entity: T): Value {
        require(version != null)
        return propMetaList.flatMap { propMap ->
            propMap.getValues(entity, { it == version })
        }.first()
    }

}
