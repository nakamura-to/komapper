package koma.expr

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExprParserTest {

    @Test
    fun gt() {
        when (val expr = ExprParser("aaa > 1").parse()) {
            is GtNode -> {
                assertTrue(expr.left is ValueNode)
                assertTrue(expr.right is LiteralNode)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun and() {
        when (val expr = ExprParser("aaa > 1 && true").parse()) {
            is AndNode -> {
                assertTrue(expr.left is GtNode)
                assertTrue(expr.right is LiteralNode)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun property() {
        when (val expr = ExprParser("aaa.age").parse()) {
            is PropertyNode -> {
                assertEquals(expr.name, "age")
                assertTrue(expr.receiver is ValueNode)
            }
            else -> throw AssertionError()
        }
    }

    @Test
    fun comma() {
        when (val expr = ExprParser("a, b, c").parse()) {
            is CommaNode -> {
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
