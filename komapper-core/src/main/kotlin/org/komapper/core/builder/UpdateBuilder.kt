package org.komapper.core.builder

import org.komapper.core.criteria.Expression
import org.komapper.core.criteria.UpdateCriteria
import org.komapper.core.entity.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Stmt
import org.komapper.core.sql.StmtBuffer

class UpdateBuilder(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: UpdateCriteria<*>
) {
    private val buf: StmtBuffer = StmtBuffer(dialect::formatValue)

    private val entityDescResolver =
        EntityDescResolver(
            entityDescFactory,
            criteria.alias,
            criteria.kClass
        )

    private val columnResolver = ColumnResolver(entityDescResolver)

    private val exprVisitor = ExpressionVisitor(buf, columnResolver)

    private val criterionVisitor =
        CriterionVisitor(
            buf,
            columnResolver
        ) { criteria ->
            SelectBuilder(
                dialect,
                entityDescFactory,
                criteria,
                entityDescResolver,
                columnResolver
            )
        }

    fun build(): Stmt {
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
                criterionVisitor.visit(where)
            }
        }
        return buf.toStmt()
    }

    private fun processAssignmentList(assignmentList: List<Pair<Expression.Property, Expression>>) {
        assignmentList.forEach { (prop, expr) ->
            exprVisitor.visit(prop) { (_, name) -> name }
            buf.append(" = ")
            exprVisitor.visit(expr)
            buf.append(", ")
        }
        buf.cutBack(2)
    }
}
