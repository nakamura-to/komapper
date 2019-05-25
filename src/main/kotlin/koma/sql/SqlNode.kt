package koma.sql

import java.util.*

class NodeList<E : SqlNode> : LinkedList<E>() {
    fun toText(): String {
        return this.joinToString(separator = "") { it.toText() }
    }
}

sealed class SqlNode {
    abstract fun toText(): String
}

data class StatementNode(val nodeList: NodeList<SqlNode>) : SqlNode() {
    override fun toText(): String {
        return nodeList.toText()
    }
}

data class SetNode(val location: SqlLocation, val keyword: String, val left: SqlNode, val right: SqlNode) : SqlNode() {
    override fun toText(): String {
        return left.toText() + keyword + right.toText()
    }
}

sealed class KeywordNode : SqlNode() {
    abstract val location: SqlLocation
    abstract val keyword: String
    abstract val nodeList: NodeList<SqlNode>
    override fun toText(): String {
        return keyword + nodeList.toText()
    }
}

data class SelectNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class FromNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class WhereNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class HavingNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class GroupByNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class OrderByNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class ForUpdateNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class OptionNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class AndNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class OrNode(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : KeywordNode()

data class BracketsNode(val node: SqlNode) : SqlNode() {
    override fun toText(): String {
        return "(${node.toText()})"
    }
}

data class IfBlockNode(
    val ifDirective: IfDirectiveNode,
    val elseifDirectives: NodeList<ElseifDirectiveNode>,
    val elseDirective: ElseDirectiveNode?,
    val endDirective: EndDirectiveNode
) : SqlNode() {
    override fun toText(): String {
        return ifDirective.toText() +
                elseifDirectives.toText() +
                (elseDirective?.toText() ?: "") +
                endDirective.toText()
    }
}

data class ForBlockNode(
    val forDirective: ForDirectiveNode,
    val endDirective: EndDirectiveNode
) : SqlNode() {
    override fun toText(): String {
        return forDirective.toText() + endDirective.toText()
    }
}

data class IfDirectiveNode(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val nodeList: NodeList<SqlNode>
) : SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class ElseifDirectiveNode(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val nodeList: NodeList<SqlNode>
) : SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class ElseDirectiveNode(val location: SqlLocation, val token: String, val nodeList: NodeList<SqlNode>) :
    SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class EndDirectiveNode(val location: SqlLocation, val token: String) : SqlNode() {
    override fun toText(): String {
        return token
    }
}

data class ForDirectiveNode(
    val location: SqlLocation,
    val token: String,
    val identifier: String,
    val expression: String,
    val nodeList: NodeList<SqlNode>
) : SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class ExpandDirectiveNode(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val node: SqlNode,
    val nodeList: NodeList<SqlNode>
) :
    SqlNode() {
    override fun toText(): String {
        return token + node.toText() + nodeList.toText()
    }
}

data class BindValueDirectiveNode(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val node: SqlNode,
    val nodeList: NodeList<SqlNode>
) :
    SqlNode() {
    override fun toText(): String {
        return token + node.toText() + nodeList.toText()
    }
}

data class EmbeddedValueDirectiveNode(val location: SqlLocation, val token: String, val expression: String) :
    SqlNode() {
    override fun toText(): String {
        return token
    }
}

data class LiteralValueDirectiveNode(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val node: SqlNode,
    val nodeList: NodeList<SqlNode>
) :
    SqlNode() {
    override fun toText(): String {
        return token + node.toText() + nodeList.toText()
    }
}

sealed class TokenNode : SqlNode() {
    abstract val token: String
    override fun toText(): String {
        return token
    }

}

data class CommentNode(override val token: String) : TokenNode()
data class WordNode(override val token: String) : TokenNode()
data class WhitespacesNode(override val token: String) : TokenNode() {
    companion object {
        val map = mapOf(
            '\u0009'.toString() to WhitespacesNode('\u0009'.toString()),
            '\u000B'.toString() to WhitespacesNode('\u000B'.toString()),
            '\u000C'.toString() to WhitespacesNode('\u000C'.toString()),
            '\u001C'.toString() to WhitespacesNode('\u001C'.toString()),
            '\u001D'.toString() to WhitespacesNode('\u001D'.toString()),
            '\u001E'.toString() to WhitespacesNode('\u001E'.toString()),
            '\u001F'.toString() to WhitespacesNode('\u001F'.toString()),
            '\u0020'.toString() to WhitespacesNode('\u0020'.toString()),
            " " to WhitespacesNode(" "),
            "  " to WhitespacesNode("  "),
            "   " to WhitespacesNode("   "),
            "    " to WhitespacesNode("    "),
            "\t" to WhitespacesNode("\t"),
            "\t\t" to WhitespacesNode("\t\t")
        )

        fun of(token: String): WhitespacesNode {
            return map.getOrElse(token) { WhitespacesNode(token) }
        }
    }
}

data class OtherNode(override val token: String) : TokenNode() {
    companion object {
        private val map = mapOf(
            "," to OtherNode(","),
            "=" to OtherNode("="),
            ">" to OtherNode(">"),
            "<" to OtherNode("<"),
            "-" to OtherNode("-"),
            "+" to OtherNode("+"),
            "*" to OtherNode("*"),
            "/" to OtherNode("/"),
            "(" to OtherNode("("),
            ")" to OtherNode(")"),
            ";" to OtherNode(";")
        )

        fun of(token: String): OtherNode {
            return map.getOrElse(token) { OtherNode(token) }
        }
    }
}


