package org.komapper.core.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultSqlRewriterTest {

    private val rewriter = DefaultSqlRewriter()

    @Test
    fun rewriteForPagination() {
        val t = template<Pair<String, Int>>("""
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
        """.trimIndent())
        val rewrittenTemplate = rewriter.rewriteForPagination(t, 5, 10)
        assertEquals("${t.sql} limit 5 offset 10", rewrittenTemplate.sql.toString())
    }

    @Test
    fun rewriteForCount() {
        val t = template<Pair<String, Int>>("""
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
        """.trimIndent())
        val rewrittenTemplate = rewriter.rewriteForCount(t)
        assertEquals("select count(*) from (${t.sql}) t_", rewrittenTemplate.sql.toString())
    }
}
