package org.komapper.core.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultSqlRewriterTest {

    private val rewriter = DefaultSqlRewriter()

    @Test
    fun rewriteForPagination() {
        val sql = """
            select
                name, age
            from
                person
            where
                name = /* name */'test'
                /*%if age != null */
                and
                age > /*^ literal */0
                /*%end */
            order by
                name, /*# embedded */
            for update
        """.trimIndent()
        val rewrittenSql = rewriter.rewriteForPagination(sql, 5, 10)
        assertEquals("$sql limit 5 offset 10", rewrittenSql.toString())
    }

    @Test
    fun rewriteForCount() {
        val sql = """
            select
                name, age
            from
                person
            where
                name = /* name */'test'
                /*%if age != null */
                and
                age > /*^ literal */0
                /*%end */
            order by
                name, /*# embedded */
            for update
        """.trimIndent()
        val rewrittenSql = rewriter.rewriteForCount(sql)
        assertEquals("select count(*) from ($sql) t_", rewrittenSql.toString())
    }
}
