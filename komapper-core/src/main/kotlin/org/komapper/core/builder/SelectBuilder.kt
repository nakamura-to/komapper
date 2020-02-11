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
    parentColumnNameResolver: ColumnNameResolver? = null
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

    private val columnNameResolver =
        ColumnNameResolver(entityDescResolver, parentColumnNameResolver)

    private val conditionBuilder =
        ConditionBuilder(
            buf,
            criteria.alias,
            columnNameResolver
        ) { criteria ->
            SelectBuilder(
                dialect,
                entityDescFactory,
                criteria,
                entityDescResolver,
                columnNameResolver
            )
        }

    fun build(expand: Boolean = true): Sql {
        buf.append("select ")
        if (criteria.distinct) {
            buf.append("distinct ")
        }
        if (expand) {
            columnNameResolver.values.forEach { buf.append("$it, ") }
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
                conditionBuilder.build(where)
            }
            if (orderBy.isNotEmpty()) {
                buf.append(" order by ")
                orderBy.forEach { item ->
                    buf.append(columnNameResolver[item.prop]).append(" ${item.sort}, ")
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
            conditionBuilder.build(joinCriteria.on)
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
