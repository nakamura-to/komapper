package org.komapper.core.criteria

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

class ConditionBuilder(
    private val buf: SqlBuffer,
    private val alias: Alias,
    private val columnNameResolver: ColumnNameResolver,
    private val newSelectBuilder: (SelectCriteria<*>) -> SelectBuilder
) {

    fun build(criterionList: List<Criterion>) {
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
                is Criterion.And -> processLogicalBinaryOp("and", index, c.criteria)
                is Criterion.Or -> processLogicalBinaryOp("or", index, c.criteria)
                is Criterion.Not -> processNotOp(c.criteria)
                is Criterion.Between -> processBetweenOp(c.prop, c.range)
                is Criterion.Exists -> processExistsOp("exists", c.criteria)
                is Criterion.NotExists -> processExistsOp("not exists", c.criteria)
            }
            buf.append(" and ")
        }
        buf.cutBack(5)
    }

    private fun processLogicalBinaryOp(op: String, index: Int, criterionList: List<Criterion>) {
        if (index > 0) {
            buf.cutBack(5).append(" $op ")
        }
        buf.append("(")
        build(criterionList)
        buf.append(")")
    }

    private fun processNotOp(criterionList: List<Criterion>) {
        buf.append("not (")
        build(criterionList)
        buf.append(")")
    }

    private fun processBinaryOp(op: String, prop: AliasProperty<*, *>, obj: Any?) {
        buf.append(columnNameResolver[prop])
        when {
            op == "=" && obj == null -> buf.append(" is null")
            op == "<>" && obj == null -> buf.append(" is not null")
            else -> {
                when (obj) {
                    is KProperty1<*, *> -> {
                        val columnName = columnNameResolver[alias[obj]]
                        buf.append(" $op ").append(columnName)
                    }
                    is AliasProperty<*, *> -> {
                        val columnName = columnNameResolver[obj]
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
        buf.append(columnNameResolver[prop])
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
        buf.append("(${columnNameResolver[prop1]}, ${columnNameResolver[prop2]})")
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
        buf.append("(${columnNameResolver[prop1]}, ${columnNameResolver[prop2]}, ${columnNameResolver[prop3]})")
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
        buf.append(columnNameResolver[prop])
            .append(" between ")
            .bind(Value(range.first, prop.kProperty1.returnType))
            .append(" and ")
            .bind(Value(range.second, prop.kProperty1.returnType))
    }

    private fun processExistsOp(op: String, criteria: SelectCriteria<*>) {
        buf.append(" $op (")
        val builder = newSelectBuilder(criteria)
        val sql = builder.build(false)
        buf.append(sql)
        buf.append(")")
    }
}
