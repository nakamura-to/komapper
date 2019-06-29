package org.komapper.core.it

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.OptimisticLockException

@ExtendWith(Env::class)
class BatchDeleteTest {

    @Test
    fun test(db: Db) {
        val list = db.select<Employee>()
        db.batchDelete(list)
        assertEquals(0, db.select<Employee>().size)
    }

    @Test
    fun optimisticException(db: Db) {
        val list = db.select<Employee>()
        db.batchDelete(list)
        assertThrows<OptimisticLockException> { db.batchDelete(list) }
    }

}
