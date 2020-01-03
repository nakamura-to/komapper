package org.komapper.jdbc.postgresql

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException

@ExtendWith(Env::class)
class UpdateTest {

    @Test
    fun test(db: Db) {
        val address = db.findById<Address>(1)!!
        db.update(address.copy(street = "a"))
    }

    @Test
    fun optimisticException(db: Db) {
        val address = db.findById<Address>(1)!!
        db.update(address)
        assertThrows<OptimisticLockException> { db.update(address) }
    }

    @Test
    fun uniqueConstraintException(db: Db) {
        val department = db.findById<Department>(1)!!
        assertThrows<UniqueConstraintException> { db.update(department.copy(departmentNo = 20)) }
    }
}
