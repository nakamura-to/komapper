package org.komapper.core.builder

import org.komapper.core.criteria.InsertCriteria
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer

class InsertBuilder(
    dialect: Dialect,
    entityDescFactory: EntityDescFactory,
    private val criteria: InsertCriteria<*>
) {
    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)

    private val entityDescResolver =
        EntityDescResolver(
            entityDescFactory,
            criteria.alias,
            criteria.kClass
        )

    private val columnResolver = ColumnResolver(entityDescResolver)

    private val exprVisitor = ExpressionVisitor(buf, columnResolver)

    fun build(): Sql {
        val entityDesc = entityDescResolver[criteria.alias]
        buf.append("insert into ${entityDesc.tableName} (")
        with(criteria) {
            if (values.isNotEmpty()) {
                values.forEach { (prop, _) ->
                    exprVisitor.visit(prop) { (_, name) -> name }
                    buf.append(", ")
                }
                buf.cutBack(2)
            }
            buf.append(") values (")
            if (values.isNotEmpty()) {
                values.forEach { (_, expr) ->
                    exprVisitor.visit(expr)
                    buf.append(", ")
                }
                buf.cutBack(2)
            }
        }
        buf.append(")")
        return buf.toSql()
    }
}
