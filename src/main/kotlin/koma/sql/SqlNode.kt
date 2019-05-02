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

data class Statement(val nodeList: NodeList<SqlNode>) : SqlNode() {
    override fun toText(): String {
        return nodeList.toText()
    }
}

data class Set(val location: SqlLocation, val keyword: String, val left: SqlNode, val right: SqlNode) : SqlNode() {
    override fun toText(): String {
        return left.toText() + keyword + right.toText()
    }
}

sealed class Keyword : SqlNode() {
    abstract val location: SqlLocation
    abstract val keyword: String
    abstract val nodeList: NodeList<SqlNode>
    override fun toText(): String {
        return keyword + nodeList.toText()
    }
}

data class Select(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class From(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class Where(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class Having(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class GroupBy(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class OrderBy(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class ForUpdate(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class Option(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class And(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class Or(
    override val location: SqlLocation,
    override val keyword: String,
    override val nodeList: NodeList<SqlNode>
) : Keyword()

data class Brackets(val node: SqlNode) : SqlNode() {
    override fun toText(): String {
        return "(${node.toText()})"
    }
}

data class IfBlock(
    val ifDirective: IfDirective,
    val elseifDirectives: NodeList<ElseifDirective>,
    val elseDirective: ElseDirective?,
    val endDirective: EndDirective
) : SqlNode() {
    override fun toText(): String {
        return ifDirective.toText() +
                elseifDirectives.toText() +
                (elseDirective?.toText() ?: "") +
                endDirective.toText()
    }
}

data class ForBlock(
    val forDirective: ForDirective,
    val endDirective: EndDirective
) : SqlNode() {
    override fun toText(): String {
        return forDirective.toText() + endDirective.toText()
    }
}

data class IfDirective(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val nodeList: NodeList<SqlNode>
) : SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class ElseifDirective(
    val location: SqlLocation,
    val token: String,
    val expression: String,
    val nodeList: NodeList<SqlNode>
) : SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class ElseDirective(val location: SqlLocation, val token: String, val nodeList: NodeList<SqlNode>) : SqlNode() {
    override fun toText(): String {
        return token + nodeList.toText()
    }
}

data class EndDirective(val location: SqlLocation, val token: String) : SqlNode() {
    override fun toText(): String {
        return token
    }
}

data class ForDirective(
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

data class ExpandDirective(
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

data class BindValueDirective(
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

data class EmbeddedValueDirective(val location: SqlLocation, val token: String, val expression: String) : SqlNode() {
    override fun toText(): String {
        return token
    }
}

data class LiteralValueDirective(
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

sealed class Token : SqlNode() {
    abstract val token: String
    override fun toText(): String {
        return token
    }

}

data class Comment(override val token: String) : Token()
data class Word(override val token: String) : Token()
data class Whitespaces(override val token: String) : Token() {
    companion object {
        val map = mapOf(
            '\u0009'.toString() to Whitespaces('\u0009'.toString()),
            '\u000B'.toString() to Whitespaces('\u000B'.toString()),
            '\u000C'.toString() to Whitespaces('\u000C'.toString()),
            '\u001C'.toString() to Whitespaces('\u001C'.toString()),
            '\u001D'.toString() to Whitespaces('\u001D'.toString()),
            '\u001E'.toString() to Whitespaces('\u001E'.toString()),
            '\u001F'.toString() to Whitespaces('\u001F'.toString()),
            '\u0020'.toString() to Whitespaces('\u0020'.toString()),
            " " to Whitespaces(" "),
            "  " to Whitespaces("  "),
            "   " to Whitespaces("   "),
            "    " to Whitespaces("    "),
            "\t" to Whitespaces("\t"),
            "\t\t" to Whitespaces("\t\t")
        )

        fun of(token: String): Whitespaces {
            return map.getOrElse(token) { Whitespaces(token) }
        }
    }
}

data class Other(override val token: String) : Token() {
    companion object {
        private val map = mapOf(
            "," to Other(","),
            "=" to Other("="),
            ">" to Other(">"),
            "<" to Other("<"),
            "-" to Other("-"),
            "+" to Other("+"),
            "*" to Other("*"),
            "/" to Other("/"),
            "(" to Other("("),
            ")" to Other(")"),
            ";" to Other(";")
        )

        fun of(token: String): Other {
            return map.getOrElse(token) { Other(token) }
        }
    }
}


