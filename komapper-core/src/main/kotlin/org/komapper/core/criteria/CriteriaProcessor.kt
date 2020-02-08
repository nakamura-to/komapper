package org.komapper.core.criteria

import kotlin.reflect.KProperty1
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

    private val entityDescMap: Map<Alias, EntityDesc<*>> =
        listOf(criteria.alias to entityDescFactory.get(criteria.kClass)).plus(
            criteria.joins.map {
                it.alias to entityDescFactory.get(it.type)
            }
        ).toMap()

    private val qualifiedColumnNameMap: Map<AliasProperty<*, *>, String> =
        entityDescMap.entries.flatMap { (alias, entityDesc) ->
            entityDesc.leafPropDescList.map { propDesc ->
                AliasProperty(alias, propDesc.prop) to "${alias.name}.${propDesc.columnName}"
            }
        }.toMap()

    override val leafPropDescList: List<PropDesc> =
        entityDescMap.values.flatMap { it.leafPropDescList }

    fun buildSelect(): Sql {
        buf.append("select ")
        if (criteria.distinct) {
            buf.append("distinct ")
        }
        qualifiedColumnNameMap.values.forEach { buf.append("$it, ") }
        val entityDesc = entityDescMap[criteria.alias] ?: error("The entityDesc not found.")
        buf.cutBack(2).append(" from ${entityDesc.tableName} ${criteria.alias.name}")
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
                orderBy.forEach { item ->
                    buf.append(resolveColumnName(item.prop)).append(" ${item.sort}, ")
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

    // TODO
    private fun resolveColumnName(prop: AliasProperty<*, *>): String {
        return qualifiedColumnNameMap[prop]
            ?: error("The column name is not found for the property \"${prop.name}\".")
    }

    private fun processJoinList(joinCriteriaList: List<JoinCriteria<*, *>>) {
        for (joinCriteria in joinCriteriaList) {
            when (joinCriteria.kind) {
                JoinKind.INNER -> buf.append(" inner join ")
                JoinKind.LEFT -> buf.append(" left join ")
            }
            val entityDesc = entityDescMap[joinCriteria.alias] ?: error("EntityDesc not found.")
            buf.append("${entityDesc.tableName} ${joinCriteria.alias.name} on (")
            visitCriterion(joinCriteria.on)
            buf.append(")")
        }
    }

    private fun visitCriterion(criterionList: List<Criterion>) {
        criterionList.forEachIndexed { index, c ->
            when (c) {
                is Criterion.Eq -> processBinaryOp("=", c.prop, c.value)
                is Criterion.Ne -> processBinaryOp("<>", c.prop, c.value)
                is Criterion.Gt -> processBinaryOp(">", c.prop, c.value)
                is Criterion.Ge -> processBinaryOp(">=", c.prop, c.value)
                is Criterion.Lt -> processBinaryOp("<", c.prop, c.value)
                is Criterion.Le -> processBinaryOp("<=", c.prop, c.value)
                is Criterion.Like -> processBinaryOp("like", c.prop, c.value)
                is Criterion.NotLike -> processBinaryOp("not like", c.prop, c.value)
                is Criterion.In -> processInOp("in", c.prop, c.values)
                is Criterion.NotIn -> processInOp("not in", c.prop, c.values)
                is Criterion.In2 -> processIn2Op("in", c.prop1, c.prop2, c.values)
                is Criterion.NotIn2 -> processIn2Op("not in", c.prop1, c.prop2, c.values)
                is Criterion.In3 -> processIn3Op("in", c.prop1, c.prop2, c.prop3, c.values)
                is Criterion.NotIn3 -> processIn3Op("not in", c.prop1, c.prop2, c.prop3, c.values)
                is Criterion.And -> visitLogicalBinaryOp("and", index, c.criteria)
                is Criterion.Or -> visitLogicalBinaryOp("or", index, c.criteria)
                is Criterion.Not -> visitNotOp(c.criteria)
                is Criterion.Between -> processBetweenOp(c.prop, c.range)
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

    private fun processBinaryOp(op: String, prop: AliasProperty<*, *>, obj: Any?) {
        buf.append(resolveColumnName(prop))
        when {
            op == "=" && obj == null -> buf.append(" is null")
            op == "<>" && obj == null -> buf.append(" is not null")
            else -> {
                when (obj) {
                    is KProperty1<*, *> -> {
                        val aliasProperty = if (obj is AliasProperty<*, *>) obj else criteria.alias[obj]
                        val columnName = resolveColumnName(aliasProperty)
                        buf.append(" $op ").append(columnName)
                    }
                    else -> {
                        val value = Value(obj, prop.returnType)
                        buf.append(" $op ").bind(value)
                    }
                }
            }
        }
    }

    private fun processInOp(op: String, prop: AliasProperty<*, *>, values: Iterable<*>) {
        buf.append(resolveColumnName(prop))
        buf.append(" $op (")
        val kClass = prop.returnType.jvmErasure
        var counter = 0
        for (v in values) {
            if (++counter > 1) buf.append(", ")
            buf.bind(Value(v, kClass))
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processIn2Op(
        op: String,
        prop1: AliasProperty<*, *>,
        prop2: AliasProperty<*, *>,
        values: Iterable<Pair<*, *>>
    ) {
        buf.append("(${resolveColumnName(prop1)}, ${resolveColumnName(prop2)})")
        buf.append(" $op (")
        val kClass1 = prop1.returnType.jvmErasure
        val kClass2 = prop2.returnType.jvmErasure
        var counter = 0
        for ((f, s) in values) {
            if (++counter > 1) buf.append(", ")
            buf.append("(")
            buf.bind(Value(f, kClass1))
            buf.append(", ")
            buf.bind(Value(s, kClass2))
            buf.append(")")
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processIn3Op(
        op: String,
        prop1: AliasProperty<*, *>,
        prop2: AliasProperty<*, *>,
        prop3: AliasProperty<*, *>,
        values: Iterable<Triple<*, *, *>>
    ) {
        buf.append("(${resolveColumnName(prop1)}, ${resolveColumnName(prop2)}, ${resolveColumnName(prop3)})")
        buf.append(" $op (")
        val kClass1 = prop1.returnType.jvmErasure
        val kClass2 = prop2.returnType.jvmErasure
        val kClass3 = prop3.returnType.jvmErasure
        var counter = 0
        for ((f, s, t) in values) {
            if (++counter > 1) buf.append(", ")
            buf.append("(")
            buf.bind(Value(f, kClass1))
            buf.append(", ")
            buf.bind(Value(s, kClass2))
            buf.append(", ")
            buf.bind(Value(t, kClass3))
            buf.append(")")
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processBetweenOp(prop: AliasProperty<*, *>, range: Pair<*, *>) {
        buf.append(resolveColumnName(prop))
            .append(" between ")
            .bind(Value(range.first, prop.returnType))
            .append(" and ")
            .bind(Value(range.second, prop.returnType))
    }

    override fun new(leafValues: Map<PropDesc, Any?>): List<Any> {
        return entityDescMap.entries.map { (_, entityDesc) ->
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
