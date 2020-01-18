package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.desc.PropDesc
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

class CriteriaProcessor(
    dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: Criteria<*>
) : MultiEntityDesc {

    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)

    private val entityDescList: List<EntityDesc<*>> =
        listOf(entityDescFactory.get(criteria.kClass)) + criteria.joins.map {
            entityDescFactory.get(it.type)
        }

    private val tableAliases: List<String> = entityDescList.mapIndexed { index, _ -> "t${index}_" }

    private val qualifiedColumnMap: Map<KProperty1<*, *>, String> = entityDescList.mapIndexed { index, entityMeta ->
        tableAliases[index] to entityMeta
    }.flatMap { (alias, entityMeta) ->
        entityMeta.leafPropDescList.map { it.prop to "$alias.${it.columnName}" }
    }.toMap()

    override val leafPropDescList: List<PropDesc> = entityDescList.flatMap { it.leafPropDescList }

    fun buildSelect(): Sql {
        buf.append("select ")
        qualifiedColumnMap.forEach { (_, columnName) -> buf.append("$columnName, ") }
        buf.cutBack(2).append(" from ${entityDescList[0].tableName} ${tableAliases[0]}")
        with(criteria) {
            if (joins.isNotEmpty()) {
                processJoinList(joins)
            }
            if (where.isNotEmpty()) {
                buf.append(" where ")
                visitCriterion(where)
            }
            if (orderBy.isNotEmpty()) {
                buf.append(" order by ")
                orderBy.forEach { (prop, sort) ->
                    buf.append(resolveColumnName(prop)).append(" $sort, ")
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

    private fun resolveColumnName(prop: KProperty1<*, *>): String {
        return qualifiedColumnMap[prop]
            ?: error(
                "The property \"${prop.name}\" is not found " +
                        "in the class \"${prop.javaField?.declaringClass?.name}\"."
            )
    }

    private fun processJoinList(joinList: List<Join<*, *>>) {
        for ((i, join) in joinList.withIndex()) {
            when (join.kind) {
                JoinKind.INNER -> buf.append(" inner join ")
                JoinKind.LEFT -> buf.append(" left join ")
            }
            val tableIndex = i + 1
            val meta = entityDescList[tableIndex]
            buf.append("${meta.tableName} ${tableAliases[tableIndex]} on (")
            visitCriterion(join.on)
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
                is Criterion.In2 -> processInOp("in", criterion.props, criterion.values)
                is Criterion.NotIn2 -> processInOp("not in", criterion.props, criterion.values)
                is Criterion.In3 -> processInOp("in", criterion.props, criterion.values)
                is Criterion.NotIn3 -> processInOp("not in", criterion.props, criterion.values)
                is Criterion.And -> visitLogicalBinaryOp("and", index, criterion.criteria)
                is Criterion.Or -> visitLogicalBinaryOp("or", index, criterion.criteria)
                is Criterion.Not -> visitNotOp(criterion.criteria)
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

    private fun processInOp(op: String, props: Pair<KProperty1<*, *>, KProperty1<*, *>>, values: Iterable<Pair<*, *>>) {
        val (first, second) = props
        buf.append("(${resolveColumnName(first)}, ${resolveColumnName(second)})")
        buf.append(" $op (")
        val firstType = first.returnType.jvmErasure
        val secondType = second.returnType.jvmErasure
        var counter = 0
        for ((f, s) in values) {
            if (++counter > 1) buf.append(", ")
            buf.append("(")
            buf.bind(Value(f, firstType))
            buf.append(", ")
            buf.bind(Value(s, secondType))
            buf.append(")")
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processInOp(
        op: String,
        props: Triple<KProperty1<*, *>, KProperty1<*, *>, KProperty1<*, *>>,
        values: Iterable<Triple<*, *, *>>
    ) {
        val (first, second, third) = props
        buf.append("(${resolveColumnName(first)}, ${resolveColumnName(second)}, ${resolveColumnName(third)})")
        buf.append(" $op (")
        val firstType = first.returnType.jvmErasure
        val secondType = second.returnType.jvmErasure
        val thirdType = third.returnType.jvmErasure
        var counter = 0
        for ((f, s, t) in values) {
            if (++counter > 1) buf.append(", ")
            buf.append("(")
            buf.bind(Value(f, firstType))
            buf.append(", ")
            buf.bind(Value(s, secondType))
            buf.append(", ")
            buf.bind(Value(t, thirdType))
            buf.append(")")
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

    override fun new(leafValues: Map<PropDesc, Any?>): List<Any> {
        return entityDescList.map { entityMeta ->
            entityMeta.new(leafValues)
        }
    }

    override fun associate(entity: Any, joinedEntities: List<Any>) {
        joinedEntities.zip(criteria.joins).forEach { (joinedEntity, join) ->
            val block = join.association
            EmptyScope.block(entity, joinedEntity)
        }
    }
}
