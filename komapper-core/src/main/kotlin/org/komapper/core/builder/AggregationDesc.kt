package org.komapper.core.builder

import org.komapper.core.criteria.Alias
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.PropDesc

interface AggregationDesc {

    val entityDescMap: Map<Alias, EntityDesc<*>>

    fun process(context: AggregationContext): List<Any>
}

class AggregationContext {
    private val store = mutableMapOf<Alias, MutableMap<EntityKey, EntityData>>()

    operator fun get(alias: Alias): MutableMap<EntityKey, EntityData> {
        return store.getOrPut(alias) { mutableMapOf() }
    }

    fun isEmpty() = store.isEmpty()
}

class EntityKey(val entityDesc: EntityDesc<*>, val properties: Map<PropDesc, Any?>) {
    private val idValues = if (entityDesc.idList.isEmpty()) {
        entityDesc.idList.map { properties[it] }
    } else {
        properties.values
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityKey

        if (entityDesc != other.entityDesc) return false
        if (idValues != other.idValues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityDesc.hashCode()
        result = 31 * result + idValues.hashCode()
        return result
    }
}

class EntityData(private val key: EntityKey) {
    val associations = AggregationContext()

    fun new(): Any = key.entityDesc.new(key.properties)

    fun isEmpty() = key.properties.values.all { it == null }

    fun associate(alias: Alias, data: EntityData) {
        val keyAndData = associations[alias]
        keyAndData.putIfAbsent(data.key, data)
    }
}
