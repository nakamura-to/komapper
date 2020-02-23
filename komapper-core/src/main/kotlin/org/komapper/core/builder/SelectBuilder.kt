package org.komapper.core.builder

import org.komapper.core.criteria.Alias
import org.komapper.core.criteria.JoinCriteria
import org.komapper.core.criteria.JoinKind
import org.komapper.core.criteria.SelectCriteria
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityDescFactory
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
) : AggregationDesc {
    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)

    private val entityDescResolver =
        EntityDescResolver(
            entityDescFactory,
            criteria.alias,
            criteria.kClass,
            criteria.joins,
            parentEntityDescResolver
        )

    override val entityDescMap: Map<Alias, EntityDesc<*>>
        get() = entityDescResolver.entityDescMap

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

    override fun process(context: AggregationContext): List<Any> {
        if (context.isEmpty()) return emptyList()
        val keyAndDataMap = context[criteria.alias]
        val dataAndEntityList = keyAndDataMap.values.map { it to it.new() }
        criteria.joins.forEach { join ->
            val block = join.association
            dataAndEntityList.forEach { (data, entity) ->
                val joinedKeyAndDataMap = data.associations[join.alias]
                val joinedEntities = joinedKeyAndDataMap.values.filter { !it.isEmpty() }.map { it.new() }
                EmptyScope.block(entity, joinedEntities)
            }
        }
        return dataAndEntityList.map { it.second }
    }
}
