package koma.expr

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExprEvaluatorTest {

    data class Person(val id: Int, val name: String)

    @Suppress("UNUSED")
    class Hello {
        fun say(name: String): String {
            return "hello $name"
        }

        fun say(name: String, message: String): String {
            return "hello $name, $message"
        }
    }

    class LiteralTest {
        @Test
        fun nullLiteral() {
            val ctx = mapOf("a" to null)
            val result = ExprEvaluator().eval("a", ctx)
            assertEquals(null, result)
        }

        @Test
        fun trueLiteral() {
            val ctx = mapOf("a" to true)
            val result = ExprEvaluator().eval("a", ctx)
            assertEquals(true, result)
        }

        @Test
        fun falseLiteral() {
            val ctx = mapOf("a" to false)
            val result = ExprEvaluator().eval("a", ctx)
            assertEquals(false, result)
        }

        @Test
        fun stringLiteral() {
            val ctx = mapOf("a" to "abc")
            val result = ExprEvaluator().eval("a", ctx)
            assertEquals("abc", result)
        }
    }

    class ComparisonOperatorTest {
        @Test
        fun eq1() {
            val ctx = mapOf("a" to 1)
            val result = ExprEvaluator().eval("a == 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun eq2() {
            val ctx = mapOf("a" to 2)
            val result = ExprEvaluator().eval("a == 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun ne1() {
            val ctx = mapOf("a" to 0)
            val result = ExprEvaluator().eval("a != 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun ne2() {
            val ctx = mapOf("a" to 1)
            val result = ExprEvaluator().eval("a != 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun ge1() {
            val ctx = mapOf("a" to 2)
            val result = ExprEvaluator().eval("a >= 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun ge2() {
            val ctx = mapOf("a" to 2)
            val result = ExprEvaluator().eval("a >= 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun ge3() {
            val ctx = mapOf("a" to 0)
            val result = ExprEvaluator().eval("a >= 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun gt1() {
            val ctx = mapOf("a" to 2)
            val result = ExprEvaluator().eval("a > 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun gt2() {
            val ctx = mapOf("a" to 1)
            val result = ExprEvaluator().eval("a > 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun gt3() {
            val ctx = mapOf("a" to 0)
            val result = ExprEvaluator().eval("a > 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun le1() {
            val ctx = mapOf("a" to 2)
            val result = ExprEvaluator().eval("a <= 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun le2() {
            val ctx = mapOf("a" to 1)
            val result = ExprEvaluator().eval("a <= 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun le3() {
            val ctx = mapOf("a" to 0)
            val result = ExprEvaluator().eval("a <= 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun lt1() {
            val ctx = mapOf("a" to 2)
            val result = ExprEvaluator().eval("a < 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun lt2() {
            val ctx = mapOf("a" to 1)
            val result = ExprEvaluator().eval("a < 1", ctx)
            assertFalse(result is Boolean && result)
        }

        @Test
        fun lt3() {
            val ctx = mapOf("a" to 0)
            val result = ExprEvaluator().eval("a < 1", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun `Cannot compare because the left operand is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a > 1", ctx) }
            println(exception)
        }

        @Test
        fun `Cannot compare because the right operand is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("1 > a", ctx) }
            println(exception)
        }

        @Test
        fun `Cannot compare because the operands are not comparable to each other`() {
            val ctx = mapOf("a" to "string")
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a > 1", ctx) }
            println(exception)
        }

    }

    class LogicalOperatorTest {
        @Test
        fun not_true() {
            val ctx = mapOf("a" to false)
            val result = ExprEvaluator().eval("!a", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun not_false() {
            val ctx = mapOf("a" to true)
            val result = ExprEvaluator().eval("!a", ctx)
            assertTrue(result is Boolean && !result)
        }

        @Test
        fun and_true() {
            val ctx = mapOf("a" to true)
            val result = ExprEvaluator().eval("a && true", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun and_false() {
            val ctx = mapOf("a" to false)
            val result = ExprEvaluator().eval("a && true", ctx)
            assertTrue(result is Boolean && !result)
        }

        @Test
        fun or_true() {
            val ctx = mapOf("a" to true)
            val result = ExprEvaluator().eval("a || false", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun or_false() {
            val ctx = mapOf("a" to false)
            val result = ExprEvaluator().eval("a || false", ctx)
            assertTrue(result is Boolean && !result)
        }

        @Test
        fun `Cannot perform the logical operator because the left operand is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a && true", ctx) }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the right operand is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("true && a", ctx) }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because either operand is not boolean`() {
            val ctx = mapOf("a" to "string")
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("true && a", ctx) }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("!a", ctx) }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is not Boolean`() {
            val ctx = mapOf("a" to "string")
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("!a", ctx) }
            println(exception)
        }
    }

    class ValueTest {
        @Test
        fun `The value cannot be resolved`() {
            val ctx = emptyMap<String, Any?>()
            val result = ExprEvaluator().eval("a", ctx)
            assertNull(result)
        }
    }

    class PropertyTest {
        @Test
        fun property() {
            val ctx = mapOf("p" to Person(1, "aaa"))
            val result = ExprEvaluator().eval("p.name", ctx)
            assertEquals("aaa", result)
        }

        @Test
        fun extensionProperty() {
            val extensions = listOf(String::lastIndex)
            val ctx = mapOf("a" to "abc")
            val result = ExprEvaluator(extensions).eval("a.lastIndex", ctx)
            assertEquals(2, result)
        }

        @Test
        fun `The receiver of the property is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a.name", ctx) }
            println(exception)
        }

        @Test
        fun `The property is not found`() {
            val ctx = mapOf("a" to "string")
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a.notFound", ctx) }
            println(exception)
        }
    }

    class FunctionTest {
        @Test
        fun function_1parameter() {
            val ctx = mapOf("h" to Hello(), "w" to "world")
            val result = ExprEvaluator().eval("h.say(w)", ctx)
            assertEquals("hello world", result)
        }

        @Test
        fun function_2parameter() {
            val ctx = mapOf("h" to Hello(), "w" to "world", "m" to "good luck")
            val result = ExprEvaluator().eval("h.say(w, m)", ctx)
            assertEquals("hello world, good luck", result)
        }

        @Test
        fun extensionFunction() {
            val extensionFunctions = listOf(String::isBlank)
            val ctx = mapOf("s" to "")
            val result = ExprEvaluator(extensionFunctions).eval("s.isBlank()", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun `Call an extension function when the receiver is null`() {
            val extensionFunctions = listOf(String::isNullOrEmpty)
            val ctx = mapOf("s" to null)
            val result = ExprEvaluator(extensionFunctions).eval("s.isNullOrEmpty()", ctx)
            assertTrue(result is Boolean && result)
        }

        @Test
        fun `The receiver of the function is null`() {
            val ctx = mapOf("a" to null)
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a.hello()", ctx) }
            println(exception)
        }

        @Test
        fun `The function is not found`() {
            val ctx = mapOf("a" to "string")
            val exception = assertThrows<ExprException> { ExprEvaluator().eval("a.notFound()", ctx) }
            println(exception)
        }

    }
}