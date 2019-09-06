package org.komapper.core.it

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException

@ExtendWith(Env::class)
class BatchUpdateTest {

    @Test
    fun test(db: Db) {
        val address1 = db.findById<Address>(1)!!
        val address2 = db.findById<Address>(2)!!
        db.batchUpdate(listOf(address1.copy(street = "a"), address2.copy(street = "b")))
    }

    @Test
    fun optimisticException(db: Db) {
        val address = db.findById<Address>(1)!!
        db.update(address)
        assertThrows<OptimisticLockException> { db.batchUpdate(listOf(address)) }
    }

    @Test
    fun uniqueConstraintException(db: Db) {
        val department = db.findById<Department>(1)!!
        assertThrows<UniqueConstraintException> { db.batchUpdate(listOf(department.copy(departmentNo = 20))) }
    }
}
