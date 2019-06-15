package org.komapper.query

import org.komapper.criteria.Criteria
import org.komapper.criteria.Criterion
import org.komapper.meta.EntityMeta
import org.komapper.sql.SqlBuffer
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

class CriteriaTraversal<T>(private val entityMeta: EntityMeta<T>, private val buf: SqlBuffer) {

    fun run(criteria: Criteria) {
        with(criteria) {
            if (whereScope.criterionList.isNotEmpty()) {
                buf.append(" where ")
                visit(whereScope.criterionList)
            }
            if (orderByScope.items.isNotEmpty()) {
                buf.append(" order by ")
                orderByScope.items.forEach { (prop, sort) ->
                    buf.append(resolveColumnName(prop)).append(" $sort, ")
                }
                buf.cutBack(2)
            }
            limit?.let { buf.append(" limit $limit") }
            offset?.let { buf.append(" offset $offset") }
        }
    }

    private fun visit(criterionList: List<Criterion>) {
        criterionList.forEachIndexed { index, criterion ->
            when (criterion) {
                is Criterion.Eq -> visitBinaryOp("=", criterion.prop, criterion.value)
                is Criterion.Ne -> visitBinaryOp("<>", criterion.prop, criterion.value)
                is Criterion.Gt -> visitBinaryOp(">", criterion.prop, criterion.value)
                is Criterion.Ge -> visitBinaryOp(">=", criterion.prop, criterion.value)
                is Criterion.Lt -> visitBinaryOp("<", criterion.prop, criterion.value)
                is Criterion.Le -> visitBinaryOp("<=", criterion.prop, criterion.value)
                is Criterion.Like -> visitBinaryOp("like", criterion.prop, criterion.value)
                is Criterion.NotLike -> visitBinaryOp("not like", criterion.prop, criterion.value)
                is Criterion.In -> visitInOp("in", criterion.prop, criterion.values)
                is Criterion.NotIn -> visitInOp("not in", criterion.prop, criterion.values)
                is Criterion.And -> visitLogicalBinaryOp("and", index, criterion.criterionList)
                is Criterion.Or -> visitLogicalBinaryOp("or", index, criterion.criterionList)
                is Criterion.Not -> visitNotOp(criterion.criterionList)
                is Criterion.Between -> visitBetweenOp(criterion.prop, criterion.range)
            }
            buf.append(" and ")
        }
        buf.cutBack(5)
    }

    private fun visitBinaryOp(op: String, prop: KProperty1<*, *>, value: Any?) {
        buf.append(resolveColumnName(prop)).append(" $op ").bind(value to prop.returnType.jvmErasure)
    }

    private fun visitInOp(op: String, prop: KProperty1<*, *>, values: Iterable<*>) {
        buf.append(resolveColumnName(prop))
        buf.append(" $op (")
        val type = prop.returnType.jvmErasure
        var counter = 0
        for (v in values) {
            if (++counter > 1) buf.append(", ")
            buf.bind(v to type)
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun visitLogicalBinaryOp(op: String, index: Int, criterionList: List<Criterion>) {
        if (index > 0) {
            buf.cutBack(5).append(" $op ")
        }
        buf.append("(")
        visit(criterionList)
        buf.append(")")
    }

    private fun visitNotOp(criterionList: List<Criterion>) {
        buf.append("not (")
        visit(criterionList)
        buf.append(")")
    }

    private fun visitBetweenOp(prop: KProperty1<*, *>, range: Pair<*, *>) {
        val type = prop.returnType.jvmErasure
        buf.append(resolveColumnName(prop))
            .append(" between ")
            .bind(range.first to type)
            .append(" and ")
            .bind(range.second to type)
    }

    private fun resolveColumnName(prop: KProperty1<*, *>): String {
        val propMeta = entityMeta.propMap[prop]
            ?: error(
                "The property \"${prop.name}\" is not found " +
                        "in the class \"${prop.javaField?.declaringClass?.name}\"."
            )
        return propMeta.columnName
    }

}
