package org.komapper.it

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import org.komapper.OptimisticLockException

@ExtendWith(Env::class)
class BatchDeleteTest {

    @Test
    fun test(db: Db) {
        val list = db.query<Employee>()
        db.batchDelete(list)
        assertEquals(0, db.query<Employee>().size)
    }

    @Test
    fun optimisticException(db: Db) {
        val list = db.query<Employee>()
        db.batchDelete(list)
        assertThrows<OptimisticLockException> { db.batchDelete(list) }
    }

}
