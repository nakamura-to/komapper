package org.komapper.core.builder

import java.util.UUID
import org.komapper.core.criteria.Alias
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.PropDesc

interface AggregationDesc {
    val fetchedEntityDescMap: Map<Alias, EntityDesc<*>>

    fun aggregate(context: AggregationContext): List<Any>
}

class AggregationContext {
    private val store = mutableMapOf<Alias, MutableMap<EntityKey, EntityData>>()

    operator fun get(alias: Alias): MutableMap<EntityKey, EntityData> {
        return store.getOrPut(alias) { mutableMapOf() }
    }

    fun isEmpty() = store.isEmpty()
}

class EntityKey(val entityDesc: EntityDesc<*>, idList: List<Any?>) {
    private val identifier: Any = if (idList.isEmpty()) {
        UUID.randomUUID()!!
    } else {
        idList
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityKey

        if (entityDesc != other.entityDesc) return false
        if (identifier != other.identifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityDesc.hashCode()
        result = 31 * result + identifier.hashCode()
        return result
    }
}

class EntityData(private val key: EntityKey, private val properties: Map<PropDesc, Any?>) {
    val associations = AggregationContext()

    fun new(): Any = key.entityDesc.new(properties)

    fun isEmpty() = properties.values.all { it == null }

    fun associate(alias: Alias, data: EntityData) {
        val keyAndData = associations[alias]
        keyAndData.putIfAbsent(data.key, data)
    }
}
