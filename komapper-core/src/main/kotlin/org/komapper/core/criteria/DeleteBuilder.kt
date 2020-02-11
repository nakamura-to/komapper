package org.komapper.core.criteria

import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer

class DeleteBuilder(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: DeleteCriteria<*>
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
        buf.append("delete from")
        val entityDesc = entityDescResolver[criteria.alias]
        buf.append(" ${entityDesc.tableName} ${criteria.alias.name}")
        with(criteria) {
            if (where.isNotEmpty()) {
                buf.append(" where ")
                conditionBuilder.build(where)
            }
        }
        return buf.toSql()
    }
}
