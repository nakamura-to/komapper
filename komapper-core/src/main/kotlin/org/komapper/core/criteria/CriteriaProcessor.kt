package org.komapper.core.criteria

import org.komapper.core.jdbc.Dialect
import org.komapper.core.meta.EntityMeta
import org.komapper.core.meta.EntityMetaFactory
import org.komapper.core.meta.PropMeta
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

class CriteriaProcessor(
    dialect: Dialect,
    private val entityMetaFactory: EntityMetaFactory,
    private val criteria: Criteria
) : MultiEntityMeta {

    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)

    private val entityMetaList: List<EntityMeta<*>> =
        listOf(entityMetaFactory.get(criteria.type)) + criteria.joins.map {
            entityMetaFactory.get(it.type)
        }

    private val tableAliases: List<String> = entityMetaList.mapIndexed { index, _ -> "t${index}_" }

    private val qualifiedColumnMap: Map<KProperty1<*, *>, String> = entityMetaList.mapIndexed { index, entityMeta ->
        tableAliases[index] to entityMeta
    }.flatMap { (alias, entityMeta) ->
        entityMeta.leafPropMetaList.map { it.prop to "$alias.${it.columnName}" }
    }.toMap()

    override val leafPropMetaList: List<PropMeta<*, *>> = entityMetaList.flatMap { it.leafPropMetaList }

    fun buildSelect(): Sql {
        buf.append("select ")
        qualifiedColumnMap.forEach { (_, columnName) -> buf.append("$columnName, ") }
        buf.cutBack(2).append(" from ${entityMetaList[0].tableName} ${tableAliases[0]}")
        with(criteria) {
            if (joins.isNotEmpty()) {
                processJoinList(joins)
            }
            if (where.criterionList.isNotEmpty()) {
                buf.append(" where ")
                visitCriterion(where.criterionList)
            }
            if (orderBy.items.isNotEmpty()) {
                buf.append(" order by ")
                orderBy.items.forEach { (prop, sort) ->
                    buf.append(resolveColumnName(prop)).append(" $sort, ")
                }
                buf.cutBack(2)
            }
            limit?.let { buf.append(" limit $limit") }
            offset?.let { buf.append(" offset $offset") }
            forUpdate?.let {
                buf.append(" for update")
                if (forUpdate.nowait) {
                    buf.append(" nowait")
                }
            }
        }

        return buf.toSql()
    }

    private fun resolveColumnName(prop: KProperty1<*, *>): String {
        return qualifiedColumnMap[prop]
            ?: error(
                "The property \"${prop.name}\" is not found " +
                        "in the class \"${prop.javaField?.declaringClass?.name}\"."
            )
    }

    private fun processJoinList(joinList: List<Join>) {
        for ((i, join) in joinList.withIndex()) {
            when (join.kind) {
                JoinKind.INNER -> buf.append(" inner join ")
                JoinKind.LEFT -> buf.append(" left join ")
            }
            val tableIndex = i + 1
            val meta = entityMetaList[tableIndex]
            buf.append("${meta.tableName} ${tableAliases[tableIndex]} on (")
            visitCriterion(join.onScope.criterionList)
            buf.append(")")
        }
    }

    private fun visitCriterion(criterionList: List<Criterion>) {
        criterionList.forEachIndexed { index, criterion ->
            when (criterion) {
                is Criterion.Eq -> processBinaryOp("=", criterion.prop, criterion.value)
                is Criterion.Ne -> processBinaryOp("<>", criterion.prop, criterion.value)
                is Criterion.Gt -> processBinaryOp(">", criterion.prop, criterion.value)
                is Criterion.Ge -> processBinaryOp(">=", criterion.prop, criterion.value)
                is Criterion.Lt -> processBinaryOp("<", criterion.prop, criterion.value)
                is Criterion.Le -> processBinaryOp("<=", criterion.prop, criterion.value)
                is Criterion.Like -> processBinaryOp("like", criterion.prop, criterion.value)
                is Criterion.NotLike -> processBinaryOp("not like", criterion.prop, criterion.value)
                is Criterion.In -> processInOp("in", criterion.prop, criterion.values)
                is Criterion.NotIn -> processInOp("not in", criterion.prop, criterion.values)
                is Criterion.And -> visitLogicalBinaryOp("and", index, criterion.criterionList)
                is Criterion.Or -> visitLogicalBinaryOp("or", index, criterion.criterionList)
                is Criterion.Not -> visitNotOp(criterion.criterionList)
                is Criterion.Between -> processBetweenOp(criterion.prop, criterion.range)
            }
            buf.append(" and ")
        }
        buf.cutBack(5)
    }

    private fun visitLogicalBinaryOp(op: String, index: Int, criterionList: List<Criterion>) {
        if (index > 0) {
            buf.cutBack(5).append(" $op ")
        }
        buf.append("(")
        visitCriterion(criterionList)
        buf.append(")")
    }

    private fun visitNotOp(criterionList: List<Criterion>) {
        buf.append("not (")
        visitCriterion(criterionList)
        buf.append(")")
    }

    private fun processBinaryOp(op: String, prop: KProperty1<*, *>, obj: Any?) {
        buf.append(resolveColumnName(prop))
        when {
            op == "=" && obj == null -> buf.append(" is null")
            op == "<>" && obj == null -> buf.append(" is not null")
            else -> {
                when (obj) {
                    is KProperty1<*, *> -> {
                        buf.append(" $op ").append(resolveColumnName(obj))
                    }
                    else -> {
                        val value = Value(obj, prop.returnType)
                        buf.append(" $op ").bind(value)
                    }
                }
            }
        }
    }

    private fun processInOp(op: String, prop: KProperty1<*, *>, values: Iterable<*>) {
        buf.append(resolveColumnName(prop))
        buf.append(" $op (")
        val type = prop.returnType.jvmErasure
        var counter = 0
        for (v in values) {
            if (++counter > 1) buf.append(", ")
            buf.bind(Value(v, type))
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processBetweenOp(prop: KProperty1<*, *>, range: Pair<*, *>) {
        buf.append(resolveColumnName(prop))
            .append(" between ")
            .bind(Value(range.first, prop.returnType))
            .append(" and ")
            .bind(Value(range.second, prop.returnType))
    }

    override fun new(leafValues: Map<PropMeta<*, *>, Any?>): List<Any> {
        return entityMetaList.map { entityMeta ->
            entityMeta.new(leafValues) as Any
        }
    }

    fun associate(entity: Any, joinedEntities: List<Any>) {
        joinedEntities.zip(criteria.joins).forEach { (joinedEntity, join) ->
            join.block(entity, joinedEntity)
        }
    }
}

