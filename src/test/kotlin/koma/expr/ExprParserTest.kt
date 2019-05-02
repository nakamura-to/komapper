package koma.expr

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExprParserTest {

    @Test
    fun gt() {
        when (val expr = ExprParser("aaa > 1").parse()) {
            is Gt -> {
                assertTrue(expr.left is Value)
                assertTrue(expr.right is Literal)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun and() {
        when (val expr = ExprParser("aaa > 1 && true").parse()) {
            is And -> {
                assertTrue(expr.left is Gt)
                assertTrue(expr.right is Literal)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun property() {
        when (val expr = ExprParser("aaa.age").parse()) {
            is Property -> {
                assertEquals(expr.name, "age")
                assertTrue(expr.receiver is Value)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun comma() {
        when (val expr = ExprParser("a, b, c").parse()) {
            is Comma -> {
                assertEquals(3, expr.nodeList.size)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun `The operand is not found`() {
        val exception = assertThrows<ExprException> { ExprParser("aaa >").parse() }
        println(exception)
    }

    @Test
    fun `The illegal number literal is found`() {
        val exception = assertThrows<ExprException> { ExprParser("1 + 1a").parse() }
        println(exception)
    }

    @Test
    fun `The close bracket is not found`() {
        val exception = assertThrows<ExprException> { ExprParser("aaa(bbb").parse() }
        println(exception)
    }

    @Test
    fun `The token is not supported`() {
        val exception = assertThrows<ExprException> { ExprParser("aaa * bbb").parse() }
        println(exception)
    }

}