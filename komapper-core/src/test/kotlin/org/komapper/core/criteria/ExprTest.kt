package org.komapper.core.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ExprTest {

    data class Person(val id: Int, val name: String)

    @Test
    fun plus() {
        assertEquals(Expr.Plus(Expr.wrap(Person::id), Expr.wrap(1)), Person::id + 1)
    }

    @Test
    fun minus() {
        assertEquals(Expr.Minus(Expr.wrap(Person::id), Expr.wrap(1)), Person::id - 1)
    }

    @Test
    fun concat() {
        assertEquals(Expr.Concat(Expr.wrap(Person::name), Expr.wrap("a")), Person::name `||` "a")
        assertEquals(Expr.Concat(Expr.wrap("a"), Expr.wrap(Person::name)), "a" `||` Person::name)
    }

    @Test
    fun complex() {
        val e = Person::id + 1 - 2 + 3
        assertEquals(
            Expr.Plus(
                Expr.Minus(Expr.Plus(Expr.wrap(Person::id), Expr.wrap(1)), Expr.wrap(2)),
                Expr.wrap(3)
            ), e
        )
    }
}
