package org.komapper.it

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import org.komapper.OptimisticLockException
import org.komapper.UniqueConstraintException

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

    @Test
    fun uniqueConstraintException(db: Db) {
        db.transaction {
            val department = db.findById<Department>(1)!!
            assertThrows<UniqueConstraintException> { db.update(department.copy(departmentNo = 20)) }
        }
    }

}
