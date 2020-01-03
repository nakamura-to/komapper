package org.komapper.jdbc.postgresql

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.UniqueConstraintException

@ExtendWith(Env::class)
class BatchMergeTest {

    @Test
    fun keys(db: Db) {
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 0),
            Department(6, 10, "DEVELOPMENT", "KYOTO", 0)
        )
        db.batchMerge(departments, Department::departmentNo)
        Assertions.assertEquals(departments[0], db.findById<Department>(5))
        Assertions.assertNull(db.findById<Department>(6))
        Assertions.assertEquals(departments[1].copy(departmentId = 1), db.findById<Department>(1))
    }

    @Test
    fun noKeys(db: Db) {
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 0),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 0)
        )
        db.batchMerge(departments)
        Assertions.assertEquals(departments[0], db.findById<Department>(5))
        Assertions.assertEquals(departments[1], db.findById<Department>(1))
    }

    @Test
    fun uniqueConstraintException(db: Db) {
        val department = db.findById<Department>(1)!!
        assertThrows<UniqueConstraintException> { db.batchMerge(listOf(department.copy(departmentId = 2))) }
    }
}
