package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

class DeleteProcessor(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: DeleteCriteria<*>,
    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue),
    private val parentEntityDescMap: Map<Alias, EntityDesc<*>> = emptyMap(),
    private val parentColumnNameMap: Map<AliasProperty<*, *>, String> = emptyMap()
) {

    private val entityDescMap: Map<Alias, EntityDesc<*>> =
        (parentEntityDescMap + listOf(criteria.alias to entityDescFactory.get(criteria.kClass))).toMap()

    private val columnNameMap: Map<AliasProperty<*, *>, String> =
        parentColumnNameMap + (
                entityDescMap.entries.flatMap { (alias, entityDesc) ->
                    entityDesc.leafPropDescList.map { propDesc ->
                        AliasProperty(alias, propDesc.prop) to "${alias.name}.${propDesc.columnName}"
                    }
                }.toMap())

    fun buildDelete(): Sql {
        appendSql()
        return buf.toSql()
    }

    private fun appendSql(expand: Boolean = true) {
        buf.append("delete from")
        val entityDesc = entityDescMap[criteria.alias] ?: error("The entityDesc not found.")
        buf.append(" ${entityDesc.tableName} ${criteria.alias.name}")
        with(criteria) {
            if (where.isNotEmpty()) {
                buf.append(" where ")
                visitCriterion(where)
            }
        }
    }

    private fun resolveColumnName(prop: AliasProperty<*, *>): String {
        return columnNameMap[prop]
            ?: error(
                "The column name is not found for the property " +
                        "\"${prop.kProperty1.javaField?.declaringClass?.name}.${prop.kProperty1.name}\". " +
                        "Is the alias \"${prop.alias.name}\" for the property correct?"
            )
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
                is Criterion.Exists -> processExists("exists", c.criteria)
                is Criterion.NotExists -> processExists("not exists", c.criteria)
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
                        val columnName = resolveColumnName(criteria.alias[obj])
                        buf.append(" $op ").append(columnName)
                    }
                    is AliasProperty<*, *> -> {
                        val columnName = resolveColumnName(obj)
                        buf.append(" $op ").append(columnName)
                    }
                    else -> {
                        val value = Value(obj, prop.kProperty1.returnType)
                        buf.append(" $op ").bind(value)
                    }
                }
            }
        }
    }

    private fun processInOp(op: String, prop: AliasProperty<*, *>, values: Iterable<*>) {
        buf.append(resolveColumnName(prop))
        buf.append(" $op (")
        val kClass = prop.kProperty1.returnType.jvmErasure
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
        val kClass1 = prop1.kProperty1.returnType.jvmErasure
        val kClass2 = prop2.kProperty1.returnType.jvmErasure
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
        val kClass1 = prop1.kProperty1.returnType.jvmErasure
        val kClass2 = prop2.kProperty1.returnType.jvmErasure
        val kClass3 = prop3.kProperty1.returnType.jvmErasure
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
            .bind(Value(range.first, prop.kProperty1.returnType))
            .append(" and ")
            .bind(Value(range.second, prop.kProperty1.returnType))
    }

    private fun processExists(op: String, criteria: Criteria<*>) {
        buf.append(" $op (")
        val processor = CriteriaProcessor(dialect, entityDescFactory, criteria, buf, entityDescMap, columnNameMap)
        processor.appendSql(false)
        buf.append(")")
    }
}
