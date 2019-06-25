package org.komapper.it

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import org.komapper.OptimisticLockException

@ExtendWith(Env::class)
class DeleteTest {

    @Test
    fun test(db: Db) {
        val address = Address(100, "a", 0)
        db.insert(address)
        db.delete(address)
        assertNull(db.findById<Address>(100))
    }

    @Test
    fun optimisticException(db: Db) {
        val address = Address(100, "a", 0)
        db.insert(address)
        db.delete(address)
        assertNull(db.findById<Address>(100))
        assertThrows<OptimisticLockException> { db.delete(address) }
    }

}
