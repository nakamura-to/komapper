package org.komapper.core.it

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.UniqueConstraintException

@ExtendWith(Env::class)
class BatchInsertTest {

    @Test
    fun test(db: Db) {
        val address = Address(100, "a", 0)
        val address2 = Address(101, "a", 0)
        db.batchInsert(listOf(address, address2))
    }

    @Test
    fun uniqueConstraintException(db: Db) {
        val address = Address(100, "a", 0)
        db.insert(address)
        assertThrows<UniqueConstraintException> { db.batchInsert(listOf(address)) }
    }

    @Test
    fun sequence(db: Db) {
        val list = (0..200).map { SequenceStrategy(it, "value$it") }
        db.batchInsert(list)
        val idList = db.select<SequenceStrategy>().map { it.id }
        Assertions.assertEquals((1..201).toList(), idList)
    }

}
