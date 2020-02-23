package org.komapper.core.sql

interface TemplateRewriter {
    fun <T : Any?> rewriteForPagination(template: Template<T>, limit: Int?, offset: Int?): Template<T>
    fun rewriteForCount(template: Template<*>): Template<Int>
}

open class DefaultTemplateRewriter : TemplateRewriter {

    override fun <T : Any?> rewriteForPagination(template: Template<T>, limit: Int?, offset: Int?): Template<T> {
        val buf = StringBuilder(template.sql.length + 32)
        buf.append(template.sql)
        if (limit != null) {
            buf.append(" limit ").append(limit)
        }
        if (offset != null) {
            buf.append(" offset ").append(offset)
        }
        return template.copy(sql = buf)
    }

    override fun rewriteForCount(template: Template<*>): Template<Int> {
        val buf = StringBuilder(template.sql.length + 32)
        buf.append(template.sql)
        buf.insert(0, "select count(*) from (")
        buf.append(") t_")
        return template(buf, template.args)
    }
}
