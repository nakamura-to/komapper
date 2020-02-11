package org.komapper.core.builder

import kotlin.reflect.jvm.javaField
import org.komapper.core.criteria.AliasProperty

class ColumnNameResolver(
    entityDescResolver: EntityDescResolver,
    private val parent: ColumnNameResolver? = null
) {

    private val columnNameMap: Map<AliasProperty<*, *>, String> =
        entityDescResolver.entries.flatMap { (alias, entityDesc) ->
            entityDesc.leafPropDescList.map { propDesc ->
                AliasProperty(alias, propDesc.prop) to "${alias.name}.${propDesc.columnName}"
            }
        }.toMap()

    val values = columnNameMap.values

    operator fun get(prop: AliasProperty<*, *>): String {
        if (parent != null) {
            val name = parent.getWithoutException(prop)
            if (name != null) {
                return name
            }
        }
        return columnNameMap[prop]
            ?: error(
                "The column name is not found for the property " +
                        "\"${prop.kProperty1.javaField?.declaringClass?.name}.${prop.kProperty1.name}\". " +
                        "Is the alias \"${prop.alias.name}\" for the property correct?"
            )
    }

    private fun getWithoutException(prop: AliasProperty<*, *>): String? {
        return columnNameMap[prop]
    }
}
