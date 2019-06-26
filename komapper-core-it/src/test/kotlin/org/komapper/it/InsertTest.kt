package org.komapper.it

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import org.komapper.UniqueConstraintException

@ExtendWith(Env::class)
class InsertTest {

    @Test
    fun test(db: Db) {
        val address = Address(100, "a", 0)
        db.insert(address)
    }

    @Test
    fun uniqueConstraintException(db: Db) {
        val address = Address(100, "a", 0)
        db.insert(address)
        assertThrows<UniqueConstraintException> { db.insert(address) }
    }

    @Test
    fun sequence(db: Db) {
        for (i in 0..200) {
            val strategy = SequenceStrategy(i, "value$i")
            db.insert(strategy)
        }
        val idList = db.select<SequenceStrategy>().map { it.id }
        assertEquals((1..201).toList(), idList)
    }
}
