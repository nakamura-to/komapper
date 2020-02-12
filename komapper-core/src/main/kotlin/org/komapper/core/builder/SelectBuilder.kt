package org.komapper.core.builder

import org.komapper.core.criteria.JoinCriteria
import org.komapper.core.criteria.JoinKind
import org.komapper.core.criteria.SelectCriteria
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.desc.MultiEntityDesc
import org.komapper.core.desc.PropDesc
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer

class SelectBuilder(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: SelectCriteria<*>,
    parentEntityDescResolver: EntityDescResolver? = null,
    parentColumnResolver: ColumnResolver? = null
) : MultiEntityDesc {
    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)

    private val entityDescResolver =
        EntityDescResolver(
            entityDescFactory,
            criteria.alias,
            criteria.kClass,
            criteria.joins,
            parentEntityDescResolver
        )

    private val columnResolver =
        ColumnResolver(entityDescResolver, parentColumnResolver)

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

    fun build(expand: Boolean = true): Sql {
        buf.append("select ")
        if (criteria.distinct) {
            buf.append("distinct ")
        }
        if (expand) {
            columnResolver.values.forEach { buf.append("$it, ") }
        } else {
            buf.append("*  ")
        }
        val entityDesc = entityDescResolver[criteria.alias]
        buf.cutBack(2).append(" from ${entityDesc.tableName} ${criteria.alias.name}")
        with(criteria) {
            if (joins.isNotEmpty()) {
                buildJoinList(joins)
            }
            if (where.isNotEmpty()) {
                buf.append(" where ")
                criterionVisitor.visit(where)
            }
            if (orderBy.isNotEmpty()) {
                buf.append(" order by ")
                orderBy.forEach { item ->
                    buf.append(columnResolver[item.prop]).append(" ${item.sort}, ")
                }
                buf.cutBack(2)
            }
            limit?.let { buf.append(" limit $limit") }
            offset?.let { buf.append(" offset $offset") }
            forUpdate?.let {
                buf.append(" for update")
                if (it.nowait) {
                    buf.append(" nowait")
                }
            }
        }
        return buf.toSql()
    }

    private fun buildJoinList(joinCriteriaList: List<JoinCriteria<*, *>>) {
        for (joinCriteria in joinCriteriaList) {
            when (joinCriteria.kind) {
                JoinKind.INNER -> buf.append(" inner join ")
                JoinKind.LEFT -> buf.append(" left join ")
            }
            val entityDesc = entityDescResolver[joinCriteria.alias]
            buf.append("${entityDesc.tableName} ${joinCriteria.alias.name} on (")
            criterionVisitor.visit(joinCriteria.on)
            buf.append(")")
        }
    }

    override val leafPropDescList: List<PropDesc> =
        entityDescResolver.values.flatMap { it.leafPropDescList }

    override fun new(leafValues: Map<PropDesc, Any?>): List<Any> {
        return entityDescResolver.entries.map { (_, entityDesc) ->
            entityDesc.new(leafValues)
        }
    }

    override fun associate(entity: Any, joinedEntities: List<Any>) {
        joinedEntities.zip(criteria.joins).forEach { (joinedEntity, join) ->
            val block = join.association
            EmptyScope.block(entity, joinedEntity)
        }
    }
}
