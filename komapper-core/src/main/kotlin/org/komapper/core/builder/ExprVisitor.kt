package org.komapper.core.builder

import org.komapper.core.criteria.Expr
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

class ExprVisitor(
    private val buf: SqlBuffer,
    private val columnResolver: ColumnResolver
) {

    fun visit(expr: Expr, columnHandler: (Column) -> CharSequence = { it }) {
        when (expr) {
            is Expr.Value -> {
                val value = Value(expr.obj, expr.kClass)
                buf.bind(value)
            }
            is Expr.Property<*, *> -> {
                val column = columnResolver[expr]
                buf.append(columnHandler(column))
            }
            is Expr.Plus -> {
                buf.append("(")
                visit(expr.left)
                buf.append(" + ")
                visit(expr.right)
                buf.append(")")
            }
            is Expr.Minus -> {
                buf.append("(")
                visit(expr.left)
                buf.append(" - ")
                visit(expr.right)
                buf.append(")")
            }
            is Expr.Concat -> {
                buf.append("(")
                visit(expr.left)
                buf.append(" || ")
                visit(expr.right)
                buf.append(")")
            }
        }
    }
}
