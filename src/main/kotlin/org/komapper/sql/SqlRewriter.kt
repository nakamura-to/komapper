package org.komapper.sql

interface SqlRewriter {
    fun rewriteForPagination(template: CharSequence, limit: Int?, offset: Int?): CharSequence
    fun rewriteForCount(template: CharSequence): CharSequence
}

class DefaultSqlRewriter(private val sqlNodeFactory: SqlNodeFactory) : SqlRewriter {

    override fun rewriteForPagination(template: CharSequence, limit: Int?, offset: Int?): CharSequence {
        fun visit(buf: StringBuilder, node: SqlNode): StringBuilder = when (node) {
            is SqlNode.Statement ->
                node.nodeList.fold(buf, ::visit)
            is SqlNode.Set -> {
                val left = visit(buf, node.left)
                buf.append(node.keyword)
                visit(left, node.right)
            }
            is SqlNode.Keyword -> {
                buf.append(node.keyword)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.Token -> {
                buf.append(node.token)
            }
            is SqlNode.Paren -> {
                buf.append("(")
                visit(buf, node.node).append(")")
            }
            is SqlNode.BindValueDirective -> {
                buf.append(node.token)
                visit(buf, node.node)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.EmbeddedValueDirective -> {
                buf.append(node.token)
            }
            is SqlNode.LiteralValueDirective -> {
                buf.append(node.token)
                visit(buf, node.node)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.ExpandDirective -> {
                buf.append(node.token)
                visit(buf, node.node)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.IfBlock -> {
                visit(buf, node.ifDirective)
                node.elseifDirectives.fold(buf, ::visit)
                node.elseDirective?.let { visit(buf, it) }
                visit(buf, node.endDirective)
            }
            is SqlNode.ForBlock -> {
                visit(buf, node.forDirective)
                visit(buf, node.endDirective)
            }
            is SqlNode.IfDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.ElseifDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.ElseDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.EndDirective ->
                buf.append(node.token)
            is SqlNode.ForDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
        }

        val node = sqlNodeFactory.get(template)
        val buf = visit(StringBuilder(500), node)
        if (limit != null) {
            buf.append(" limit ").append(limit)
        }
        if (offset != null) {
            buf.append(" offset ").append(offset)
        }
        return buf
    }

    override fun rewriteForCount(template: CharSequence): CharSequence {
        fun visit(buf: StringBuilder, node: SqlNode): StringBuilder = when (node) {
            is SqlNode.Statement ->
                node.nodeList.fold(buf, ::visit)
            is SqlNode.Set -> {
                val left = visit(buf, node.left)
                buf.append(node.keyword)
                visit(left, node.right)
            }
            is SqlNode.Keyword -> {
                buf.append(node.keyword)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.Token -> {
                buf.append(node.token)
            }
            is SqlNode.Paren -> {
                buf.append("(")
                visit(buf, node.node).append(")")
            }
            is SqlNode.BindValueDirective -> {
                buf.append(node.token)
                visit(buf, node.node)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.EmbeddedValueDirective -> {
                buf.append(node.token)
            }
            is SqlNode.LiteralValueDirective -> {
                buf.append(node.token)
                visit(buf, node.node)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.ExpandDirective -> {
                buf.append(node.token)
                visit(buf, node.node)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.IfBlock -> {
                visit(buf, node.ifDirective)
                node.elseifDirectives.fold(buf, ::visit)
                node.elseDirective?.let { visit(buf, it) }
                visit(buf, node.endDirective)
            }
            is SqlNode.ForBlock -> {
                visit(buf, node.forDirective)
                visit(buf, node.endDirective)
            }
            is SqlNode.IfDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.ElseifDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.ElseDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
            is SqlNode.EndDirective ->
                buf.append(node.token)
            is SqlNode.ForDirective -> {
                buf.append(node.token)
                node.nodeList.fold(buf, ::visit)
            }
        }

        val node = sqlNodeFactory.get(template)
        val buf = visit(StringBuilder(500), node)
        buf.insert(0, "select count(*) from (")
        buf.append(") t_")
        return buf
    }
}
