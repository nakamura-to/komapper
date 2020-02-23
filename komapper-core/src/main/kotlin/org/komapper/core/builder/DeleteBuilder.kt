package org.komapper.core.builder

import org.komapper.core.criteria.DeleteCriteria
import org.komapper.core.entity.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Stmt
import org.komapper.core.sql.StmtBuffer

class DeleteBuilder(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: DeleteCriteria<*>
) {
    private val buf: StmtBuffer = StmtBuffer(dialect::formatValue)

    private val entityDescResolver =
        EntityDescResolver(
            entityDescFactory,
            criteria.alias,
            criteria.kClass
        )

    private val columnResolver = ColumnResolver(entityDescResolver)

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
        buf.append("delete from")
        val entityDesc = entityDescResolver[criteria.alias]
        buf.append(" ${entityDesc.tableName} ${criteria.alias.name}")
        with(criteria) {
            if (where.isNotEmpty()) {
                buf.append(" where ")
                criterionVisitor.visit(where)
            }
        }
        return buf.toStmt()
    }
}
