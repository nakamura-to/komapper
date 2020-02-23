package org.komapper.core.builder

import kotlin.reflect.jvm.javaField
import org.komapper.core.criteria.Expression

class ColumnResolver(
    entityDescResolver: EntityDescResolver,
    private val parent: ColumnResolver? = null
) {

    private val defaultAlias = entityDescResolver.entityDescMap.entries.first().key

    val fetchedColumns: List<Column> =
        entityDescResolver.fetchedEntityDescMap.flatMap { (alias, entityDesc) ->
            entityDesc.leafPropDescList.map { propDesc ->
                Column(alias.name, propDesc.columnName)
            }
        }

    private val columnMap: Map<Expression.Property, Column> =
        entityDescResolver.entityDescMap.flatMap { (alias, entityDesc) ->
            entityDesc.leafPropDescList.map { propDesc ->
                Expression.Property(alias, propDesc.prop) to Column(alias.name, propDesc.columnName)
            }
        }.toMap()

    operator fun get(prop: Expression.Property): Column {
        val key = if (prop.alias == null) Expression.Property(defaultAlias, prop.prop) else prop
        if (parent != null) {
            val name = parent.getWithoutException(key)
            if (name != null) {
                return name
            }
        }
        return columnMap[key]
            ?: error(
                "The column name is not found for the property " +
                        "\"${prop.prop.javaField?.declaringClass?.name}.${prop.prop.name}\". " +
                        "Is the alias of the property correct?"
            )
    }

    private fun getWithoutException(prop: Expression.Property): Column? {
        return columnMap[prop]
    }
}
