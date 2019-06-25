package org.komapper.it

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import org.komapper.OptimisticLockException

@ExtendWith(Env::class)
class UpdateTest {

    @Test
    fun test(db: Db) {
        db.transaction {
            val address = db.findById<Address>(1)!!
            db.update(address.copy(street = "a"))
        }
    }

    @Test
    fun optimisticException(db: Db) {
        db.transaction {
            val address = db.findById<Address>(1)!!
            db.update(address)
            assertThrows<OptimisticLockException> { db.update(address) }
        }
    }

}
