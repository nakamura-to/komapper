package org.komapper.sql

interface SqlRewriter {
    fun rewriteForPagination(template: CharSequence, limit: Int?, offset: Int?): CharSequence
    fun rewriteForCount(template: CharSequence): CharSequence
}

class DefaultSqlRewriter(private val sqlNodeFactory: SqlNodeFactory) : SqlRewriter {

    override fun rewriteForPagination(template: CharSequence, limit: Int?, offset: Int?): CharSequence {
        val buf = StringBuilder(template.length + 32)
        buf.append(template)
        if (limit != null) {
            buf.append(" limit ").append(limit)
        }
        if (offset != null) {
            buf.append(" offset ").append(offset)
        }
        return buf
    }

    override fun rewriteForCount(template: CharSequence): CharSequence {
        val buf = StringBuilder(template.length + 32)
        buf.append(template)
        buf.insert(0, "select count(*) from (")
        buf.append(") t_")
        return buf
    }
}
