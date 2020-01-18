package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db

@ExtendWith(Env::class)
internal class FindByIdTest(private val db: Db) {

    @Test
    fun test() {
        val address = db.findById<Address>(2)
        Assertions.assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ), address
        )
    }

    @Test
    fun idAndVersion() {
        val address = db.findById<Address>(2, 1)
        Assertions.assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ), address
        )
    }

    @Test
    fun idList() {
        val address = db.findById<CompositeKeyAddress>(listOf(2, 2))
        Assertions.assertEquals(
            CompositeKeyAddress(
                2,
                2,
                "STREET 2",
                1
            ), address
        )
    }

    @Test
    fun idListAndVersion() {
        val address = db.findById<CompositeKeyAddress>(listOf(2, 2), 1)
        Assertions.assertEquals(
            CompositeKeyAddress(
                2,
                2,
                "STREET 2",
                1
            ), address
        )
    }

    @Test
    fun embedded() {
        val employee = db.findById<Employee>(1)
        Assertions.assertNotNull(employee)
    }

    @Test
    fun nestedEmbedded() {
        val employee = db.findById<Worker>(1)
        Assertions.assertNotNull(employee)
    }
}
