package org.komapper.core.builder

import kotlin.reflect.jvm.javaField
import org.komapper.core.criteria.Expr

class ColumnResolver(
    entityDescResolver: EntityDescResolver,
    private val parent: ColumnResolver? = null
) {

    private val defaultAlias = entityDescResolver.entityDescMap.entries.first().key

    private val columnMap: Map<Expr.Property<*, *>, Column> =
        entityDescResolver.entityDescMap.flatMap { (alias, entityDesc) ->
            entityDesc.leafPropDescList.map { propDesc ->
                Expr.Property(alias, propDesc.prop) to Column(alias.name, propDesc.columnName)
            }
        }.toMap()

    val values = columnMap.values

    operator fun get(prop: Expr.Property<*, *>): Column {
        val key = if (prop.alias == null) Expr.Property(defaultAlias, prop.prop) else prop
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

    private fun getWithoutException(prop: Expr.Property<*, *>): Column? {
        return columnMap[prop]
    }
}
