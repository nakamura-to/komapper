package koma.sql

import koma.expr.ExprEvaluator

class SqlBuilder(private val evaluator: ExprEvaluator = ExprEvaluator()) {

    fun build(template: String, ctx: Map<String, Any?> = emptyMap()): Sql {
        val parser = SqlParser(template)
        val node = parser.parse()
        val state = visit(State(ctx), node)
        val buffer = state.getBuffer()
        return Sql(buffer.sql.toString(), buffer.values, buffer.log.toString())
    }

    private fun visit(state: State, node: SqlNode): State = when (node) {
        is Statement -> node.nodeList.fold(state, ::visit)
        is Set -> {
            val left = visit(state, node.left)
            state.append(node.keyword)
            visit(left, node.right)
        }
        is ForUpdate -> {
            state.append(node.keyword)
            node.nodeList.fold(state, ::visit)
        }
        is Keyword -> {
            val childState = node.nodeList.fold(State(state), ::visit)
            if (childState.available) {
                state.append(node.keyword)
                state.append(childState)
            }
            state
        }
        is Token -> {
            if (node is Word || node is Other) {
                state.available = true
            }
            state.append(node.token)
            state
        }
        is Brackets -> {
            state.available = true
            state.append("(")
            visit(state, node.node).also {
                state.append(")")
            }
        }
        is BindValueDirective -> {
            when (val value = eval(node.expression, state.ctx)) {
                is Iterable<*> -> {
                    var counter = 0
                    state.append("(")
                    for (v in value) {
                        if (++counter > 1) state.append(", ")
                        state.bind(v)
                    }
                    if (counter == 0) {
                        state.append("null")
                    }
                    state.append(")")
                }
                else -> state.bind(value)
            }
            node.nodeList.fold(state, ::visit)
        }
        is EmbeddedValueDirective -> {
            val value = eval(node.expression, state.ctx)
            val s = value?.toString()
            if (!s.isNullOrEmpty()) {
                state.available = true
                state.append(s)
            }
            state
        }
        is LiteralValueDirective -> {
            val value = eval(node.expression, state.ctx)
            val literal = toText(value)
            state.append(literal)
            node.nodeList.fold(state, ::visit)
        }
        is ExpandDirective -> {
            // TODO
            throw NotImplementedError()
        }
        is IfBlock -> {
            fun chooseNodeList(): List<SqlNode> {
                val result = eval(node.ifDirective.expression, state.ctx)
                if (result == true) {
                    return node.ifDirective.nodeList
                } else {
                    val elseIfDirective = node.elseifDirectives.find {
                        eval(it.expression, state.ctx) == true
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
        is ForBlock -> {
            val forDirective = node.forDirective
            val id = forDirective.identifier
            val expression = eval(node.forDirective.expression, state.ctx) as? Iterable<*>
                ?: throw SqlException("The expression ${forDirective.expression} is not Iterable at ${forDirective.location}")
            val it = expression.iterator()
            var s = state
            var preserved: Any? = s.ctx[id]
            var index = 0
            val idIndex = id + "_index"
            val idHasNext = id + "_has_next"
            while (it.hasNext()) {
                s.ctx[id] = it.next()
                s.ctx[idIndex] = index++
                s.ctx[idHasNext] = it.hasNext()
                s = node.forDirective.nodeList.fold(s, ::visit)
            }
            s.ctx[id] = preserved
            s.ctx.remove(idIndex)
            s.ctx.remove(idHasNext)
            s
        }
        is IfDirective,
        is ElseifDirective,
        is ElseDirective,
        is EndDirective,
        is ForDirective -> {
            throw AssertionError("unreachable")
        }
    }

    private fun eval(expression: String, ctx: Map<String, Any?>): Any? {
        return evaluator.eval(expression, ctx)
    }

    private fun toText(value: Any?): String {
        return if (value is CharSequence) "'$value'" else value.toString()
    }

}

class Buffer(capacity: Int = 200) {

    val sql = StringBuilder(capacity)
    val log = StringBuilder(capacity)
    val values = ArrayList<Any?>()

    fun append(s: CharSequence) {
        sql.append(s)
        log.append(s)
    }

    fun bind(value: Any?) {
        sql.append("?")
        log.append(toText(value))
        values.add(value)
    }
}

class State(ctx: Map<String, Any?>) {
    var available: Boolean = false
    val ctx: MutableMap<String, Any?> = HashMap(ctx)
    val buf = Buffer()

    constructor(state: State) : this(state.ctx)

    fun append(state: State) {
        buf.sql.append(state.buf.sql)
        buf.log.append(state.buf.log)
        buf.values.addAll(state.buf.values)
    }

    fun append(s: CharSequence) {
        buf.append(s)
    }

    fun bind(value: Any?) {
        buf.bind(value)
    }

    fun getBuffer(): Buffer {
        return buf
    }

}

private fun toText(value: Any?): String {
    return if (value is CharSequence) "'$value'" else value.toString()
}

data class Sql(val text: String, val values: List<Any?>, val log: String)
