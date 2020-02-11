package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

class UpdateBuilder(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: UpdateCriteria<*>
) {
    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)

    private val entityDescResolver =
        EntityDescResolver(entityDescFactory, criteria.alias, criteria.kClass)

    private val columnNameResolver = ColumnNameResolver(entityDescResolver)

    private val conditionBuilder =
        ConditionBuilder(buf, criteria.alias, columnNameResolver) { criteria ->
            SelectBuilder(dialect, entityDescFactory, criteria, entityDescResolver, columnNameResolver)
        }

    fun build(): Sql {
        buf.append("update")
        val entityDesc = entityDescResolver[criteria.alias]
        buf.append(" ${entityDesc.tableName} ${criteria.alias.name}")
        with(criteria) {
            if (set.isNotEmpty()) {
                buf.append(" set ")
                processAssignmentList(set)
            }
            if (where.isNotEmpty()) {
                buf.append(" where ")
                conditionBuilder.build(where)
            }
        }
        return buf.toSql()
    }

    private fun processAssignmentList(assignmentList: List<Pair<AliasProperty<*, *>, Any?>>) {
        assignmentList.forEach { (prop, obj) ->
            val entityDesc = entityDescResolver[prop.alias]
            val propDesc = entityDesc.propMap[prop.kProperty1]
                ?: error("The propDesc is not found for the property \"${prop.kProperty1.name}\"")
            buf.append("${propDesc.columnName} = ")
            when (obj) {
                is KProperty1<*, *> -> {
                    val columnName = columnNameResolver[criteria.alias[obj]]
                    buf.append(columnName)
                }
                is AliasProperty<*, *> -> {
                    val columnName = columnNameResolver[obj]
                    buf.append(columnName)
                }
                else -> {
                    val value = Value(obj, prop.kProperty1.returnType)
                    buf.bind(value)
                }
            }
            buf.append(", ")
        }
        buf.cutBack(2)
    }
}
