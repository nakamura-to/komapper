package org.komapper.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.komapper.expr.DefaultExprEvaluator
import org.komapper.expr.NoCacheExprNodeFactory

class DefaultSqlBuilderTest {

    private val sqlBuilder = DefaultSqlBuilder(
        { value, _ -> value.toString() },
        sqlNodeFactory = NoCacheSqlNodeFactory(),
        exprEvaluator = DefaultExprEvaluator(NoCacheExprNodeFactory())
    )

    @Test
    fun simple() {
        val template = "select * from person"
        val sql = sqlBuilder.build(template)
        assertEquals(template, sql.text)
    }

    @Test
    fun complex() {
        val template =
            "select name, age from person where name = /*name*/'test' and age > 1 order by name, age for update"
        val sql = sqlBuilder.build(template)
        assertEquals("select name, age from person where name = ? and age > 1 order by name, age for update", sql.text)
    }

    @Nested
    inner class BindValueTest {

        @Test
        fun singleValue() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to ("aaa" to String::class)))
            assertEquals("select name, age from person where name = ? and age > 1", sql.text)
            assertEquals(listOf("aaa" to String::class), sql.values)
        }

        @Test
        fun singleValue_null() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where name = ? and age > 1", sql.text)
            assertEquals(listOf(null to Any::class), sql.values)
        }

        @Test
        fun multipleValues() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to (listOf("x", "y", "z") to List::class)))
            assertEquals("select name, age from person where name in (?, ?, ?) and age > 1", sql.text)
            assertEquals(listOf("x" to String::class, "y" to String::class, "z" to String::class), sql.values)
        }

        @Test
        fun multipleValues_null() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where name in ? and age > 1", sql.text)
            assertEquals(listOf(null to Any::class), sql.values)
        }

        @Test
        fun multipleValues_empty() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to (emptyList<String>() to List::class)))
            assertEquals("select name, age from person where name in (null) and age > 1", sql.text)
            assertTrue(sql.values.isEmpty())
        }

    }

    @Nested
    inner class EmbeddedValueTest {

        @Test
        fun test() {
            val template = "select name, age from person where age > 1 /*# orderBy */"
            val sql = sqlBuilder.build(template, mapOf("orderBy" to ("order by name" to String::class)))
            assertEquals("select name, age from person where age > 1 order by name", sql.text)
        }
    }

    @Nested
    inner class LiteralValueTest {

        @Test
        fun test() {
            val template = "select name, age from person where name = /*^name*/'test' and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to ("aaa" to String::class)))
            assertEquals("select name, age from person where name = 'aaa' and age > 1", sql.text)
        }
    }

    @Nested
    inner class IfBlockTest {
        @Test
        fun if_true() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val sql = sqlBuilder.build(template, mapOf("name" to ("aaa" to String::class)))
            assertEquals("select name, age from person where name = ? and 1 = 1", sql.text)
            assertEquals(listOf("aaa" to String::class), sql.values)
        }

        @Test
        fun if_false() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person ", sql.text)
        }
    }

    @Nested
    inner class ForBlockTest {
        @Test
        fun test() {
            val template =
                "select name, age from person where /*%for i in list*/age = /*i*/0 /*%if i_has_next *//*# \"or\" */ /*%end*//*%end*/"
            val sql = sqlBuilder.build(template, mapOf("list" to (listOf(1, 2, 3) to List::class)))
            assertEquals("select name, age from person where age = ? or age = ? or age = ? ", sql.text)
            assertEquals(listOf(1 to Int::class, 2 to Int::class, 3 to Int::class), sql.values)
        }
    }

    @Nested
    inner class BracketsTest {
        @Test
        fun subQuery() {
            val template = "select name, age from (select * from person)"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from (select * from person)", sql.text)
        }
    }

}
