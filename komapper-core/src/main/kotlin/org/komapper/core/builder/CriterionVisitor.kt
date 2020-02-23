package org.komapper.core.builder

import org.komapper.core.criteria.Criterion
import org.komapper.core.criteria.Expression
import org.komapper.core.criteria.SelectCriteria
import org.komapper.core.sql.StmtBuffer

class CriterionVisitor(
    private val buf: StmtBuffer,
    columnResolver: ColumnResolver,
    private val newSelectBuilder: (SelectCriteria<*>) -> SelectBuilder
) {

    private val exprVisitor = ExpressionVisitor(buf, columnResolver)

    fun visit(criterionList: List<Criterion>) {
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
        visit(criterionList)
        buf.append(")")
    }

    private fun processNotOp(criterionList: List<Criterion>) {
        buf.append("not (")
        visit(criterionList)
        buf.append(")")
    }

    private fun processBinaryOp(op: String, left: Expression, right: Expression) {
        exprVisitor.visit(left)
        fun processRightExpr() {
            buf.append(" $op ")
            exprVisitor.visit(right)
        }
        if (op == "=") {
            if (left is Expression.Property) {
                if (right is Expression.Value && right.obj == null) {
                    buf.append(" is null")
                } else {
                    processRightExpr()
                }
            }
        } else if (op == "<>") {
            if (left is Expression.Property) {
                if (right is Expression.Value && right.obj == null) {
                    buf.append(" is not null")
                } else {
                    processRightExpr()
                }
            }
        } else {
            processRightExpr()
        }
    }

    private fun processInOp(op: String, prop: Expression, values: Iterable<Expression>) {
        exprVisitor.visit(prop)
        buf.append(" $op (")
        var counter = 0
        for (each in values) {
            if (++counter > 1) buf.append(", ")
            exprVisitor.visit(each)
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processIn2Op(
        op: String,
        prop1: Expression,
        prop2: Expression,
        values: Iterable<Pair<Expression, Expression>>
    ) {
        buf.append("(")
        exprVisitor.visit(prop1)
        buf.append(", ")
        exprVisitor.visit(prop2)
        buf.append(")")
        buf.append(" $op (")
        var counter = 0
        for ((f, s) in values) {
            if (++counter > 1) buf.append(", ")
            buf.append("(")
            exprVisitor.visit(f)
            buf.append(", ")
            exprVisitor.visit(s)
            buf.append(")")
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processIn3Op(
        op: String,
        prop1: Expression,
        prop2: Expression,
        prop3: Expression,
        values: Iterable<Triple<Expression, Expression, Expression>>
    ) {
        buf.append("(")
        exprVisitor.visit(prop1)
        buf.append(", ")
        exprVisitor.visit(prop2)
        buf.append(", ")
        exprVisitor.visit(prop3)
        buf.append(")")
        buf.append(" $op (")
        var counter = 0
        for ((f, s, t) in values) {
            if (++counter > 1) buf.append(", ")
            buf.append("(")
            exprVisitor.visit(f)
            buf.append(", ")
            exprVisitor.visit(s)
            buf.append(", ")
            exprVisitor.visit(t)
            buf.append(")")
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun processBetweenOp(prop: Expression, range: Pair<Expression, Expression>) {
        exprVisitor.visit(prop)
        buf.append(" between ")
        exprVisitor.visit(range.first)
        buf.append(" and ")
        exprVisitor.visit(range.second)
    }

    private fun processExistsOp(op: String, criteria: SelectCriteria<*>) {
        buf.append(" $op (")
        val builder = newSelectBuilder(criteria)
        val sql = builder.build(false)
        buf.append(sql)
        buf.append(")")
    }
}
