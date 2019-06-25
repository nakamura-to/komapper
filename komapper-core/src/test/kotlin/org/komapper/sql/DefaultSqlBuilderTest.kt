package org.komapper.sql

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.Value
import org.komapper.expr.DefaultExprEvaluator
import org.komapper.expr.NoCacheExprNodeFactory

class DefaultSqlBuilderTest {

    private val sqlBuilder = DefaultSqlBuilder(
        { value, _ -> if (value is CharSequence) "'$value'" else value.toString() },
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

    @Test
    fun `The expression evaluation was failed`() {
        val template =
            "select name, age from person where name = /*name.a*/'test'"
        val exception = assertThrows<SqlException> { sqlBuilder.build(template) }
        println(exception)
    }

    @Nested
    inner class BindValueTest {

        @Test
        fun singleValue() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value("aaa")))
            assertEquals("select name, age from person where name = ? and age > 1", sql.text)
            assertEquals(listOf(Value("aaa")), sql.values)
        }

        @Test
        fun singleValue_null() {
            val template = "select name, age from person where name = /*name*/'test' and age > 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where name = ? and age > 1", sql.text)
            assertEquals(listOf(Value(null, Any::class)), sql.values)
        }

        @Test
        fun multipleValues() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value(listOf("x", "y", "z"))))
            assertEquals("select name, age from person where name in (?, ?, ?) and age > 1", sql.text)
            assertEquals(listOf(Value("x"), Value("y"), Value("z")), sql.values)
        }

        @Test
        fun multipleValues_null() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from person where name in ? and age > 1", sql.text)
            assertEquals(listOf(Value(null, Any::class)), sql.values)
        }

        @Test
        fun multipleValues_empty() {
            val template = "select name, age from person where name in /*name*/('a', 'b') and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value(emptyList<String>())))
            assertEquals("select name, age from person where name in (null) and age > 1", sql.text)
            assertTrue(sql.values.isEmpty())
        }

    }

    @Nested
    inner class EmbeddedValueTest {

        @Test
        fun test() {
            val template = "select name, age from person where age > 1 /*# orderBy */"
            val sql = sqlBuilder.build(template, mapOf("orderBy" to Value("order by name")))
            assertEquals("select name, age from person where age > 1 order by name", sql.text)
        }
    }

    @Nested
    inner class LiteralValueTest {

        @Test
        fun test() {
            val template = "select name, age from person where name = /*^name*/'test' and age > 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value("aaa")))
            assertEquals("select name, age from person where name = 'aaa' and age > 1", sql.text)
        }
    }

    @Nested
    inner class IfBlockTest {
        @Test
        fun if_true() {
            val template =
                "select name, age from person where /*%if name != null*/name = /*name*/'test'/*%end*/ and 1 = 1"
            val sql = sqlBuilder.build(template, mapOf("name" to Value("aaa")))
            assertEquals("select name, age from person where name = ? and 1 = 1", sql.text)
            assertEquals(listOf(Value("aaa")), sql.values)
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
            val sql = sqlBuilder.build(template, mapOf("list" to Value(listOf(1, 2, 3))))
            assertEquals("select name, age from person where age = ? or age = ? or age = ? ", sql.text)
            assertEquals(listOf(Value(1), Value(2), Value(3)), sql.values)
        }
    }

    @Nested
    inner class ParenTest {
        @Test
        fun subQuery() {
            val template = "select name, age from (select * from person)"
            val sql = sqlBuilder.build(template)
            assertEquals("select name, age from (select * from person)", sql.text)
        }
    }

    @Nested
    inner class ExpandTest {
        @Test
        fun test() {
            val template = "select /*%expand */* from person"
            val expander: (String) -> List<String> = { prefix -> listOf("name", "age").map { "$prefix$it" } }
            val sql = sqlBuilder.build(template, expander = expander)
            assertEquals("select name, age from person", sql.text)
        }

        @Test
        fun alias() {
            val template = "select /*%expand 'p'*/* from person p"
            val expander: (String) -> List<String> = { prefix -> listOf("name", "age").map { "$prefix$it" } }
            val sql = sqlBuilder.build(template, expander = expander)
            assertEquals("select p.name, p.age from person p", sql.text)
        }

        @Test
        fun `The alias expression cannot be resolved`() {
            val template = "select /*%expand p*/* from person"
            val expander: (String) -> List<String> = { prefix -> listOf("name", "age").map { "$prefix$it" } }
            val exception = assertThrows<SqlException> { sqlBuilder.build(template, expander = expander) }
            println(exception)
        }
    }
}