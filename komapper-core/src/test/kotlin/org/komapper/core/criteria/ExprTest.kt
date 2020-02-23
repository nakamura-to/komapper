package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ExprTest {

    data class Person(val id: Int, val name: String)

    @Test
    fun plus() {
        assertEquals(Expression.Plus(Expression.wrap(Person::id), Expression.wrap(1)), expression { Person::id + 1 })
    }

    @Test
    fun minus() {
        assertEquals(Expression.Minus(Expression.wrap(Person::id), Expression.wrap(1)), expression { Person::id - 1 })
    }

    @Test
    fun concat() {
        assertEquals(Expression.Concat(Expression.wrap(Person::name), Expression.wrap("a")), expression { Person::name `||` "a" })
        assertEquals(Expression.Concat(Expression.wrap("a"), Expression.wrap(Person::name)), expression { "a" `||` Person::name })
    }

    @Test
    fun complex() {
        val e = expression { Person::id + 1 - 2 + 3 }
        assertEquals(
            Expression.Plus(
                Expression.Minus(Expression.Plus(Expression.wrap(Person::id), Expression.wrap(1)), Expression.wrap(2)),
                Expression.wrap(3)
            ), e
        )
    }
}
