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
    private val leafPropMetaList = propMetaList.flatMap { it.getLeafPropMetaList() }
    val idList = leafPropMetaList.filter { it.kind is PropKind.Id }
    val version = leafPropMetaList.find { it.kind is PropKind.Version }
    val createdAt = leafPropMetaList.find { it.kind is PropKind.CreatedAt }
    val updatedAt = leafPropMetaList.find { it.kind is PropKind.UpdatedAt }
    val columnNameMap = leafPropMetaList.associateBy { it.columnName }
    val propMap = leafPropMetaList.associateBy { it.prop }

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
        return copy(entity, valueArgs)
    }

    fun incrementVersion(entity: T): T {
        if (version == null) {
            return entity
        }
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it == version }) { _, currentValue ->
                version.inc(currentValue)
            }
        }.toMap()
        return copy(entity, valueArgs)
    }

    fun assignTimestamp(entity: T): T {
        if (createdAt == null) {
            return entity
        }
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it == createdAt }) { _, _ -> createdAt.now() }
        }.toMap()
        return copy(entity, valueArgs)
    }

    fun updateTimestamp(entity: T): T {
        if (updatedAt == null) {
            return entity
        }
        val valueArgs = propMetaList.mapNotNull { propMeta ->
            propMeta.copy(entity, { it == updatedAt }) { _, _ -> updatedAt.now() }
        }.toMap()
        return copy(entity, valueArgs)
    }

    private fun copy(entity: T, valueArgs: Map<KParameter, Any?>): T {
        val receiverArg = copy.parameters[0] to entity
        return copy.callBy(mapOf(receiverArg) + valueArgs)
    }

    fun getColumnNames(): List<String> =
        propMetaList.flatMap { it.getColumnNames { true } }

    fun getIdColumnNames(): List<String> =
        propMetaList.flatMap { meta -> meta.getColumnNames { it in idList } }

    fun getNonIdColumnNames(): List<String> =
        propMetaList.flatMap { meta -> meta.getColumnNames { it !in idList } }

    fun getValues(entity: T): List<Value> =
        propMetaList.flatMap { it.getValues(entity, { true }) }

    fun getIdValues(entity: T): List<Value> =
        propMetaList.flatMap { meta -> meta.getValues(entity, { it in idList }) }

    fun getNonIdValues(entity: T): List<Value> =
        propMetaList.flatMap { meta -> meta.getValues(entity, { it !in idList }) }

    fun getVersionValue(entity: T): Value {
        require(version != null)
        return propMetaList.flatMap { meta -> meta.getValues(entity, { it == version }) }.first()
    }

}
