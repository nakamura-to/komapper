package org.komapper.sql

import org.komapper.core.Value
import org.komapper.expr.ExprEvaluator
import kotlin.reflect.KClass

interface SqlBuilder {
    fun build(template: CharSequence, ctx: Map<String, Value> = emptyMap()): Sql
}

open class DefaultSqlBuilder(
    private val formatter: (Any?, KClass<*>) -> String,
    private val sqlNodeFactory: SqlNodeFactory,
    private val exprEvaluator: ExprEvaluator
) : SqlBuilder {

    override fun build(template: CharSequence, ctx: Map<String, Value>): Sql {
        val node = sqlNodeFactory.get(template)
        val state = visit(State(ctx), node)
        return state.toSql()
    }

    private fun visit(state: State, node: SqlNode): State = when (node) {
        is SqlNode.Statement -> node.nodeList.fold(state, ::visit)
        is SqlNode.Set -> {
            val left = visit(state, node.left)
            state.append(node.keyword)
            visit(left, node.right)
        }
        is SqlNode.Keyword.ForUpdate -> {
            state.append(node.keyword)
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.Keyword -> {
            val childState = node.nodeList.fold(State(state.ctx), ::visit)
            if (childState.available) {
                state.append(node.keyword).append(childState)
            }
            state
        }
        is SqlNode.Token -> {
            if (node is SqlNode.Token.Word || node is SqlNode.Token.Other) {
                state.available = true
            }
            state.append(node.token)
        }
        is SqlNode.Paren -> {
            state.available = true
            state.append("(")
            visit(state, node.node).append(")")
        }
        is SqlNode.BindValueDirective -> {
            val result = eval(node.expression, state.ctx)
            when (val obj = result.obj) {
                is Iterable<*> -> {
                    var counter = 0
                    state.append("(")
                    for (o in obj) {
                        if (++counter > 1) state.append(", ")
                        val value = Value(o, o?.let { it::class } ?: Any::class)
                        state.bind(value)
                    }
                    if (counter == 0) {
                        state.append("null")
                    }
                    state.append(")")
                }
                else -> state.bind(result)
            }
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.EmbeddedValueDirective -> {
            val (value) = eval(node.expression, state.ctx)
            val s = value?.toString()
            if (!s.isNullOrEmpty()) {
                state.available = true
                state.append(s)
            }
            state
        }
        is SqlNode.LiteralValueDirective -> {
            val (value, type) = eval(node.expression, state.ctx)
            val literal = formatter(value, type)
            state.append(literal)
            node.nodeList.fold(state, ::visit)
        }
        is SqlNode.ExpandDirective -> {
            // TODO
            throw NotImplementedError()
        }
        is SqlNode.IfBlock -> {
            fun chooseNodeList(): List<SqlNode> {
                val (result) = eval(node.ifDirective.expression, state.ctx)
                if (result == true) {
                    return node.ifDirective.nodeList
                } else {
                    val elseIfDirective = node.elseifDirectives.find {
                        val (r) = eval(it.expression, state.ctx)
                        r == true
                    }
                    if (elseIfDirective != null) {
                        return elseIfDirective.nodeList
                    } else {
                        if (node.elseDirective != null) {
                            return node.elseDirective.nodeList
                        } else {
                            return emptyList()
                        }
                    }
                }
            }

            val nodeList = chooseNodeList()
            nodeList.fold(state, ::visit)
        }
        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            val id = forDirective.identifier
            val (expression) = eval(node.forDirective.expression, state.ctx)
            expression as? Iterable<*>
                ?: throw SqlException("The expression ${forDirective.expression} is not Iterable at ${forDirective.location}")
            val it = expression.iterator()
            var s = state
            val preserved = s.ctx[id]
            var index = 0
            val idIndex = id + "_index"
            val idHasNext = id + "_has_next"
            while (it.hasNext()) {
                val each = it.next()
                s.ctx[id] = Value(each, each?.let { it::class } ?: Any::class)
                s.ctx[idIndex] = Value(index++, Int::class)
                s.ctx[idHasNext] = Value(it.hasNext(), Boolean::class)
                s = node.forDirective.nodeList.fold(s, ::visit)
            }
            if (preserved != null) {
                s.ctx[id] = preserved
            }
            s.ctx.remove(idIndex)
            s.ctx.remove(idHasNext)
            s
        }
        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective -> error("unreachable")
    }

    private fun eval(expression: String, ctx: Map<String, Value>): Value = exprEvaluator.eval(expression, ctx)

    inner class State(ctx: Map<String, Value>) {
        private val buf = SqlBuffer(formatter)
        val ctx: MutableMap<String, Value> = HashMap(ctx)
        var available: Boolean = false

        fun append(state: State): State {
            buf.sql.append(state.buf.sql)
            buf.log.append(state.buf.log)
            buf.values.addAll(state.buf.values)
            return this
        }

        fun append(s: CharSequence): State {
            buf.append(s)
            return this
        }

        fun bind(value: Value): State {
            buf.bind(value)
            return this
        }

        fun toSql(): Sql = buf.toSql()
    }
}
