package org.komapper.sql

import org.komapper.Value
import org.komapper.expr.ExprEvaluator
import org.komapper.jdbc.Dialect
import kotlin.reflect.KClass

private fun format(value: Any?, valueClass: KClass<*>): String {
    return value.toString()
}

interface SqlBuilder {
    fun build(template: CharSequence, ctx: Map<String, Value> = emptyMap()): Sql
}

open class DefaultSqlBuilder internal constructor(
    private val formatter: (Any?, KClass<*>) -> String = ::format,
    private val sqlNodeFactory: SqlNodeFactory = NoCacheSqlNodeFactory(),
    private val evaluator: ExprEvaluator = ExprEvaluator()
): SqlBuilder {

    constructor(
        dialect: Dialect,
        sqlNodeFactory: SqlNodeFactory,
        exprEvaluator: ExprEvaluator
    ) : this(dialect::formatValue, sqlNodeFactory, exprEvaluator)

    override fun build(template: CharSequence, ctx: Map<String, Value>): Sql {
        val node = sqlNodeFactory.get(template)
        val state = visit(State(ctx), node)
        val buffer = state.getBuffer()
        return buffer.toSql()
    }

    private fun visit(state: State, node: SqlNode): State = when (node) {
        is StatementNode -> node.nodeList.fold(state, ::visit)
        is SetNode -> {
            val left = visit(state, node.left)
            state.append(node.keyword)
            visit(left, node.right)
        }
        is ForUpdateNode -> {
            state.append(node.keyword)
            node.nodeList.fold(state, ::visit)
        }
        is KeywordNode -> {
            val childState = node.nodeList.fold(State(state), ::visit)
            if (childState.available) {
                state.append(node.keyword)
                state.append(childState)
            }
            state
        }
        is TokenNode -> {
            if (node is WordNode || node is OtherNode) {
                state.available = true
            }
            state.append(node.token)
            state
        }
        is BracketsNode -> {
            state.available = true
            state.append("(")
            visit(state, node.node).also {
                state.append(")")
            }
        }
        is BindValueDirectiveNode -> {
            val result = eval(node.expression, state.ctx)
            when (val value = result.first) {
                is Iterable<*> -> {
                    var counter = 0
                    state.append("(")
                    for (v in value) {
                        if (++counter > 1) state.append(", ")
                        state.bind(v to (if (v == null) Any::class else v::class))
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
        is EmbeddedValueDirectiveNode -> {
            val (value) = eval(node.expression, state.ctx)
            val s = value?.toString()
            if (!s.isNullOrEmpty()) {
                state.available = true
                state.append(s)
            }
            state
        }
        is LiteralValueDirectiveNode -> {
            val (value) = eval(node.expression, state.ctx)
            val literal = toText(value)
            state.append(literal)
            node.nodeList.fold(state, ::visit)
        }
        is ExpandDirectiveNode -> {
            // TODO
            throw NotImplementedError()
        }
        is IfBlockNode -> {
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
        is ForBlockNode -> {
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
                s.ctx[id] = if (each == null) null to Any::class else each to each::class
                s.ctx[idIndex] = index++ to Int::class
                s.ctx[idHasNext] = it.hasNext() to Boolean::class
                s = node.forDirective.nodeList.fold(s, ::visit)
            }
            if (preserved != null) {
                s.ctx[id] = preserved
            }
            s.ctx.remove(idIndex)
            s.ctx.remove(idHasNext)
            s
        }
        is IfDirectiveNode,
        is ElseifDirectiveNode,
        is ElseDirectiveNode,
        is EndDirectiveNode,
        is ForDirectiveNode -> {
            throw AssertionError("unreachable")
        }
    }

    private fun eval(expression: String, ctx: Map<String, Value>): Value {
        return evaluator.eval(expression, ctx)
    }

    private fun toText(value: Any?): String {
        return if (value is CharSequence) "'$value'" else value.toString()
    }

    inner class State(ctx: Map<String, Value>) {
        var available: Boolean = false
        val ctx: MutableMap<String, Value> = HashMap(ctx)
        val buf = SqlBuffer(formatter)

        constructor(state: State) : this(state.ctx)

        fun append(state: State) {
            buf.sql.append(state.buf.sql)
            buf.log.append(state.buf.log)
            buf.values.addAll(state.buf.values)
        }

        fun append(s: CharSequence) {
            buf.append(s)
        }

        fun bind(value: Value) {
            buf.bind(value)
        }

        fun getBuffer(): SqlBuffer {
            return buf
        }
    }
}
