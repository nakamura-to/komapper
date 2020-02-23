package org.komapper.core.builder

import org.komapper.core.criteria.Expression
import org.komapper.core.sql.StmtBuffer
import org.komapper.core.value.Value

class ExpressionVisitor(
    private val buf: StmtBuffer,
    private val columnResolver: ColumnResolver
) {

    fun visit(expression: Expression, columnHandler: (Column) -> CharSequence = { it }) {
        when (expression) {
            is Expression.Value -> {
                val value = Value(expression.obj, expression.kClass)
                buf.bind(value)
            }
            is Expression.Property -> {
                val column = columnResolver[expression]
                buf.append(columnHandler(column))
            }
            is Expression.Plus -> {
                buf.append("(")
                visit(expression.left)
                buf.append(" + ")
                visit(expression.right)
                buf.append(")")
            }
            is Expression.Minus -> {
                buf.append("(")
                visit(expression.left)
                buf.append(" - ")
                visit(expression.right)
                buf.append(")")
            }
            is Expression.Concat -> {
                buf.append("(")
                visit(expression.left)
                buf.append(" || ")
                visit(expression.right)
                buf.append(")")
            }
        }
    }
}
