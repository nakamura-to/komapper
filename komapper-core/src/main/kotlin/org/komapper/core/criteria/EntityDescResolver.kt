package org.komapper.core.criteria

import kotlin.reflect.KClass
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityDescFactory

class EntityDescResolver(
    entityDescFactory: EntityDescFactory,
    alias: Alias,
    entityClass: KClass<*>,
    joins: MutableList<JoinCriteria<Any, Any>>? = null,
    private val parent: EntityDescResolver? = null
) {

    private val entityDescMap: Map<Alias, EntityDesc<*>> =
                listOf(alias to entityDescFactory.get(entityClass)).plus(
                    joins?.map {
                        it.alias to entityDescFactory.get(it.type)
                    } ?: emptyList()
                ).toMap()

    val entries = entityDescMap.entries

    val values = entityDescMap.values

    operator fun get(alias: Alias): EntityDesc<*> {
        if (parent != null) {
            val name = parent.getWithoutException(alias)
            if (name != null) {
                return name
            }
        }
        return entityDescMap[alias] ?: error("The entityDesc is not found for the alias \"${alias.name}\"")
    }

    private fun getWithoutException(alias: Alias): EntityDesc<*>? {
        return entityDescMap[alias]
    }
}
