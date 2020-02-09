package org.komapper.jdbc.postgresql

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.OptimisticLockException
import org.komapper.core.criteria.delete

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

    @Test
    fun criteria(db: Db) {
        val address = Address(100, "a", 0)
        db.insert(address)
        val query = delete<Address> {
            where {
                eq(Address::addressId, 100)
            }
        }
        val count = db.delete(query)
        Assertions.assertEquals(1, count)
        assertNull(db.findById<Address>(100))
    }
}
