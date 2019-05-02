package koma.sql

abstract class SqlNodeReducer {
    protected val nodeList: NodeList<SqlNode> = NodeList()

    abstract fun reduce(): SqlNode

    open fun addNode(node: SqlNode) {
        nodeList.add(node)
    }
}

class SetReducer(private val location: SqlLocation, private val keyword: String, private val left: SqlNode) :
    SqlNodeReducer() {
    override fun reduce(): SqlNode {
        val right = nodeList.poll() ?: throw AssertionError("node not found")
        return Set(location, keyword, left, right)
    }
}

class StatementReducer : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return Statement(nodeList)
    }
}

class SelectReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return Select(location, keyword, nodeList)
    }
}

class FromReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return From(location, keyword, nodeList)
    }
}

class WhereReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return Where(location, keyword, nodeList)
    }
}

class HavingReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return Having(location, keyword, nodeList)
    }
}

class GroupByReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return GroupBy(location, keyword, nodeList)
    }
}

class OrderByReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return OrderBy(location, keyword, nodeList)
    }
}

class ForUpdateReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return ForUpdate(location, keyword, nodeList)
    }
}

class OptionReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return Option(location, keyword, nodeList)
    }
}

class AndReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return And(location, keyword, nodeList)
    }
}

class OrReducer(private val location: SqlLocation, private val keyword: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return Or(location, keyword, nodeList)
    }
}

class BindValueDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String
) :
    SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return when (val node = nodeList.poll()) {
            is Word, is Brackets -> BindValueDirective(location, token, expression, node, nodeList)
            else -> throw SqlException("The test value must follow the bind value directive at $location. node=$node")
        }
    }
}

class LiteralValueDirectiveReducer(
    private val location: SqlLocation,
    val token: String,
    private val expression: String
) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return when (val node = nodeList.poll()) {
            is Word -> LiteralValueDirective(location, token, expression, node, nodeList)
            else -> throw SqlException("The test value must follow the literal value directive at $location")
        }
    }
}

abstract class BlockReducer : SqlNodeReducer()

class IfBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is IfDirective, is ElseifDirective, is ElseDirective, is EndDirective -> super.addNode(node)
        else -> throw AssertionError(node)
    }

    override fun reduce(): SqlNode {
        val ifDirective = getIfDirective()
        val elseifDirectives = getElseifDirectives()
        val elseDirective = getElseDirective()
        val endDirect = getEndDirective()
        return IfBlock(ifDirective, elseifDirectives, elseDirective, endDirect)
    }

    private fun getIfDirective(): IfDirective {
        val node = nodeList.poll() as? IfDirective
        return node ?: throw AssertionError(node)
    }

    private fun getElseifDirectives(): NodeList<ElseifDirective> {
        val list = NodeList<ElseifDirective>()
        while (true) {
            val node = nodeList.peek() as? ElseifDirective
            if (node != null) {
                nodeList.pop()
                list.add(node)
            } else {
                return list
            }
        }
    }

    private fun getElseDirective(): ElseDirective? {
        val node = nodeList.peek() as? ElseDirective
        if (node != null) {
            nodeList.pop()
        }
        return node
    }

    private fun getEndDirective(): EndDirective {
        return when (val node = nodeList.poll()) {
            is EndDirective -> node
            is ElseifDirective -> throw SqlException("The illegal elseif directive is found at ${node.location}")
            is ElseDirective -> throw SqlException("The illegal else directive is found at ${node.location}")
            else -> throw throw SqlException("The corresponding end directive is not found at $location")
        }
    }
}

class ForBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is ForDirective, is EndDirective -> super.addNode(node)
        else -> throw AssertionError(node)
    }

    override fun reduce(): SqlNode {
        val forDirective = getForDirective()
        val endDirective = getEndDirective()
        return ForBlock(forDirective, endDirective)
    }

    private fun getForDirective(): ForDirective {
        val node = nodeList.poll() as? ForDirective
        return node ?: throw AssertionError(node)
    }

    private fun getEndDirective(): EndDirective {
        return when (val node = nodeList.poll()) {
            is EndDirective -> node
            else -> throw throw SqlException("The corresponding end directive is not found at $location")
        }
    }
}

class IfDirectiveReducer(private val location: SqlLocation, private val token: String, private val expression: String) :
    SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return IfDirective(location, token, expression, nodeList)
    }
}

class ElseifDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String
) :
    SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return ElseifDirective(location, token, expression, nodeList)
    }
}

class ElseDirectiveReducer(private val location: SqlLocation, private val token: String) : SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return ElseDirective(location, token, nodeList)
    }
}

class ForDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val identifier: String,
    private val expression: String
) :
    SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return ForDirective(location, token, identifier, expression, nodeList)
    }
}

class ExpandDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String
) :
    SqlNodeReducer() {
    override fun reduce(): SqlNode {
        return when (val node = nodeList.poll()) {
            Other("*") -> ExpandDirective(location, token, expression, node, nodeList)
            else -> throw SqlException("The token \"*\" must follow the expand directive at $location")
        }
    }
}
