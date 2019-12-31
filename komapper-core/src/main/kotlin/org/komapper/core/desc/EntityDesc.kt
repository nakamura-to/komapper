package org.komapper.core.desc

class EntityDesc<T>(
    private val dataDesc: DataDesc,
    val tableName: String
) {
    val kClass = dataDesc.kClass
    val leafPropDescList = dataDesc.getLeafPropDescList()
    val idList = leafPropDescList.filter { it.kind is PropKind.Id }
    val sequenceIdList = idList.filter { it.kind is PropKind.Id.Sequence }
    val nonIdList = leafPropDescList - idList
    val version = leafPropDescList.find { it.kind is PropKind.Version }
    val createdAt = leafPropDescList.find { it.kind is PropKind.CreatedAt }
    val updatedAt = leafPropDescList.find { it.kind is PropKind.UpdatedAt }
    val columnLabelMap = leafPropDescList.associateBy { it.columnLabel }
    val propMap = leafPropDescList.associateBy { it.prop }
    val expander: (String) -> List<String> = { prefix -> leafPropDescList.map { prefix + it.columnName } }

    fun new(leaves: Map<PropDesc, Any?>): T {
        return dataDesc.new(leaves) as T
    }

    private fun copy(
        entity: T,
        predicate: (PropDesc) -> Boolean,
        block: (PropDesc, () -> Any?) -> Any?
    ): T {
        return dataDesc.copy(entity as Any, predicate, block) as T
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
}
