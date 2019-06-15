package org.komapper.expr

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.Value

class ExprEvaluatorTest {

    private val evaluator = DefaultExprEvaluator(NoCacheExprNodeFactory())

    data class Person(val id: Int, val name: String, val age: Int?)

    @Suppress("UNUSED")
    class Hello {
        fun say(name: String): String {
            return "hello $name"
        }

        fun say(name: String, message: String): String {
            return "hello $name, $message"
        }
    }

    @Nested
    inner class LiteralTest {
        @Test
        fun nullLiteral() {
            val ctx = mapOf("a" to (null to Any::class))
            val result = evaluator.eval("a", ctx)
            assertEquals(null to Any::class, result)
        }

        @Test
        fun trueLiteral() {
            val ctx = mapOf("a" to (true to Boolean::class))
            val result = evaluator.eval("a", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun falseLiteral() {
            val ctx = mapOf("a" to (false to Boolean::class))
            val result = evaluator.eval("a", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun stringLiteral() {
            val ctx = mapOf("a" to ("abc" to String::class))
            val result = evaluator.eval("a", ctx)
            assertEquals("abc" to String::class, result)
        }
    }

    @Nested
    inner class ComparisonOperatorTest {

        @Test
        fun eq1() {
            val ctx = mapOf("a" to (1 to Int::class))
            val result = evaluator.eval("a == 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun eq2() {
            val ctx = mapOf("a" to (2 to Int::class))
            val result = evaluator.eval("a == 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun ne1() {
            val ctx = mapOf("a" to (0 to Int::class))
            val result = evaluator.eval("a != 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun ne2() {
            val ctx = mapOf("a" to (1 to Int::class))
            val result = evaluator.eval("a != 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun ge1() {
            val ctx = mapOf("a" to (2 to Int::class))
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun ge2() {
            val ctx = mapOf("a" to (2 to Int::class))
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun ge3() {
            val ctx = mapOf("a" to (0 to Int::class))
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun gt1() {
            val ctx = mapOf("a" to (2 to Int::class))
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun gt2() {
            val ctx = mapOf("a" to (1 to Int::class))
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun gt3() {
            val ctx = mapOf("a" to (0 to Int::class))
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun le1() {
            val ctx = mapOf("a" to (2 to Int::class))
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun le2() {
            val ctx = mapOf("a" to (1 to Int::class))
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun le3() {
            val ctx = mapOf("a" to (0 to Int::class))
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun lt1() {
            val ctx = mapOf("a" to (2 to Int::class))
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun lt2() {
            val ctx = mapOf("a" to (1 to Int::class))
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun lt3() {
            val ctx = mapOf("a" to (0 to Int::class))
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun `Cannot compare because the left operand is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a > 1", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the right operand is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("1 > a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the operands are not comparable to each other`() {
            val ctx = mapOf("a" to ("string" to String::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a > 1", ctx)
            }
            println(exception)
        }

    }

    @Nested
    inner class LogicalOperatorTest {
        @Test
        fun not_true() {
            val ctx = mapOf("a" to (false to Boolean::class))
            val result = evaluator.eval("!a", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun not_false() {
            val ctx = mapOf("a" to (true to Boolean::class))
            val result = evaluator.eval("!a", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun and_true() {
            val ctx = mapOf("a" to (true to Boolean::class))
            val result = evaluator.eval("a && true", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun and_false() {
            val ctx = mapOf("a" to (false to Boolean::class))
            val result = evaluator.eval("a && true", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun or_true() {
            val ctx = mapOf("a" to (true to Boolean::class))
            val result = evaluator.eval("a || false", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun or_false() {
            val ctx = mapOf("a" to (false to Boolean::class))
            val result = evaluator.eval("a || false", ctx)
            assertEquals(false to Boolean::class, result)
        }

        @Test
        fun `Cannot perform the logical operator because the left operand is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a && true", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the right operand is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("true && a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because either operand is not boolean`() {
            val ctx = mapOf("a" to ("string" to String::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("true && a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("!a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is not Boolean`() {
            val ctx = mapOf("a" to ("string" to String::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("!a", ctx)
            }
            println(exception)
        }
    }

    @Nested
    inner class ValueTest {
        @Test
        fun `The value cannot be resolved`() {
            val ctx = emptyMap<String, Value>()
            val result = evaluator.eval("a", ctx)
            assertEquals(null to Any::class, result)
        }
    }

    @Nested
    inner class PropertyTest {
        @Test
        fun property() {
            val ctx = mapOf(
                "p" to (Person(
                    1,
                    "aaa",
                    20
                ) to Person::class)
            )
            val result = evaluator.eval("p.name", ctx)
            assertEquals("aaa" to String::class, result)
        }

        @Test
        fun property_nullable() {
            val ctx = mapOf(
                "p" to (Person(
                    1,
                    "aaa",
                    null
                ) to Person::class)
            )
            val result = evaluator.eval("p.age", ctx)
            assertEquals(null to Int::class, result)
        }

        @Test
        fun extensionProperty() {
            val extensions = listOf(String::lastIndex)
            val ctx = mapOf("a" to ("abc" to String::class))
            val result = DefaultExprEvaluator(NoCacheExprNodeFactory(), extensions).eval("a.lastIndex", ctx)
            assertEquals(2 to Int::class, result)
        }

        @Test
        fun `The receiver of the property is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a.name", ctx)
            }
            println(exception)
        }

        @Test
        fun `The property is not found`() {
            val ctx = mapOf("a" to ("string" to String::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a.notFound", ctx)
            }
            println(exception)
        }
    }

    @Nested
    inner class FunctionTest {
        @Test
        fun function_1parameter() {
            val ctx = mapOf("h" to (Hello() to Hello::class), "w" to ("world" to String::class))
            val result = evaluator.eval("h.say(w)", ctx)
            assertEquals("hello world" to String::class, result)
        }

        @Test
        fun function_2parameter() {
            val ctx = mapOf(
                "h" to (Hello() to Hello::class),
                "w" to ("world" to String::class),
                "m" to ("good luck" to String::class)
            )
            val result = evaluator.eval("h.say(w, m)", ctx)
            assertEquals("hello world, good luck" to String::class, result)
        }

        @Test
        fun extensionFunction() {
            val extensionFunctions = listOf(String::isBlank)
            val ctx = mapOf("s" to ("" to String::class))
            val result = DefaultExprEvaluator(NoCacheExprNodeFactory(), extensionFunctions).eval("s.isBlank()", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun `Call an extension function when the receiver is null`() {
            val extensionFunctions = listOf(String::isNullOrEmpty)
            val ctx = mapOf("s" to (null to Any::class))
            val result =
                DefaultExprEvaluator(NoCacheExprNodeFactory(), extensionFunctions).eval("s.isNullOrEmpty()", ctx)
            assertEquals(true to Boolean::class, result)
        }

        @Test
        fun `The receiver of the function is null`() {
            val ctx = mapOf("a" to (null to Any::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a.hello()", ctx)
            }
            println(exception)
        }

        @Test
        fun `The function is not found`() {
            val ctx = mapOf("a" to ("string" to String::class))
            val exception = assertThrows<ExprException> {
                evaluator
                    .eval("a.notFound()", ctx)
            }
            println(exception)
        }

    }
}
