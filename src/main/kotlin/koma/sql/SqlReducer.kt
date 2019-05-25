package koma.sql

abstract class SqlReducer {
    protected val nodeList: NodeList<SqlNode> = NodeList()

    abstract fun reduce(): SqlNode

    open fun addNode(node: SqlNode) {
        nodeList.add(node)
    }
}

class SetReducer(private val location: SqlLocation, private val keyword: String, private val left: SqlNode) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        val right = nodeList.poll() ?: throw AssertionError("node not found")
        return SetNode(location, keyword, left, right)
    }
}

class StatementReducer : SqlReducer() {
    override fun reduce(): SqlNode {
        return StatementNode(nodeList)
    }
}

class SelectReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return SelectNode(location, keyword, nodeList)
    }
}

class FromReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return FromNode(location, keyword, nodeList)
    }
}

class WhereReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return WhereNode(location, keyword, nodeList)
    }
}

class HavingReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return HavingNode(location, keyword, nodeList)
    }
}

class GroupByReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return GroupByNode(location, keyword, nodeList)
    }
}

class OrderByReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return OrderByNode(location, keyword, nodeList)
    }
}

class ForUpdateReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return ForUpdateNode(location, keyword, nodeList)
    }
}

class OptionReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return OptionNode(location, keyword, nodeList)
    }
}

class AndReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return AndNode(location, keyword, nodeList)
    }
}

class OrReducer(private val location: SqlLocation, private val keyword: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return OrNode(location, keyword, nodeList)
    }
}

class BindValueDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String
) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        return when (val node = nodeList.poll()) {
            is WordNode, is BracketsNode -> BindValueDirectiveNode(location, token, expression, node, nodeList)
            else -> throw SqlException("The test value must follow the bind value directive at $location. node=$node")
        }
    }
}

class LiteralValueDirectiveReducer(
    private val location: SqlLocation,
    val token: String,
    private val expression: String
) : SqlReducer() {
    override fun reduce(): SqlNode {
        return when (val node = nodeList.poll()) {
            is WordNode -> LiteralValueDirectiveNode(location, token, expression, node, nodeList)
            else -> throw SqlException("The test value must follow the literal value directive at $location")
        }
    }
}

abstract class BlockReducer : SqlReducer()

class IfBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is IfDirectiveNode, is ElseifDirectiveNode, is ElseDirectiveNode, is EndDirectiveNode -> super.addNode(node)
        else -> throw AssertionError(node)
    }

    override fun reduce(): SqlNode {
        val ifDirective = getIfDirective()
        val elseifDirectives = getElseifDirectives()
        val elseDirective = getElseDirective()
        val endDirect = getEndDirective()
        return IfBlockNode(ifDirective, elseifDirectives, elseDirective, endDirect)
    }

    private fun getIfDirective(): IfDirectiveNode {
        val node = nodeList.poll() as? IfDirectiveNode
        return node ?: throw AssertionError(node)
    }

    private fun getElseifDirectives(): NodeList<ElseifDirectiveNode> {
        val list = NodeList<ElseifDirectiveNode>()
        while (true) {
            val node = nodeList.peek() as? ElseifDirectiveNode
            if (node != null) {
                nodeList.pop()
                list.add(node)
            } else {
                return list
            }
        }
    }

    private fun getElseDirective(): ElseDirectiveNode? {
        val node = nodeList.peek() as? ElseDirectiveNode
        if (node != null) {
            nodeList.pop()
        }
        return node
    }

    private fun getEndDirective(): EndDirectiveNode {
        return when (val node = nodeList.poll()) {
            is EndDirectiveNode -> node
            is ElseifDirectiveNode -> throw SqlException("The illegal elseif directive is found at ${node.location}")
            is ElseDirectiveNode -> throw SqlException("The illegal else directive is found at ${node.location}")
            else -> throw throw SqlException("The corresponding end directive is not found at $location")
        }
    }
}

class ForBlockReducer(private val location: SqlLocation) : BlockReducer() {

    override fun addNode(node: SqlNode) = when (node) {
        is ForDirectiveNode, is EndDirectiveNode -> super.addNode(node)
        else -> throw AssertionError(node)
    }

    override fun reduce(): SqlNode {
        val forDirective = getForDirective()
        val endDirective = getEndDirective()
        return ForBlockNode(forDirective, endDirective)
    }

    private fun getForDirective(): ForDirectiveNode {
        val node = nodeList.poll() as? ForDirectiveNode
        return node ?: throw AssertionError(node)
    }

    private fun getEndDirective(): EndDirectiveNode {
        return when (val node = nodeList.poll()) {
            is EndDirectiveNode -> node
            else -> throw throw SqlException("The corresponding end directive is not found at $location")
        }
    }
}

class IfDirectiveReducer(private val location: SqlLocation, private val token: String, private val expression: String) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        return IfDirectiveNode(location, token, expression, nodeList)
    }
}

class ElseifDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String
) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        return ElseifDirectiveNode(location, token, expression, nodeList)
    }
}

class ElseDirectiveReducer(private val location: SqlLocation, private val token: String) : SqlReducer() {
    override fun reduce(): SqlNode {
        return ElseDirectiveNode(location, token, nodeList)
    }
}

class ForDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val identifier: String,
    private val expression: String
) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        return ForDirectiveNode(location, token, identifier, expression, nodeList)
    }
}

class ExpandDirectiveReducer(
    private val location: SqlLocation,
    private val token: String,
    private val expression: String
) :
    SqlReducer() {
    override fun reduce(): SqlNode {
        return when (val node = nodeList.poll()) {
            OtherNode("*") -> ExpandDirectiveNode(location, token, expression, node, nodeList)
            else -> throw SqlException("The token \"*\" must follow the expand directive at $location")
        }
    }
}
