package org.komapper.core.it

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.UniqueConstraintException

@ExtendWith(Env::class)
class MergeTest {

    @Test
    fun insert_keys(db: Db) {
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        db.merge(department, Department::departmentNo)
        val department2 = db.findById<Department>(5)
        assertEquals(department, department2)
    }

    @Test
    fun insert_noKeys(db: Db) {
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        db.merge(department)
        val department2 = db.findById<Department>(5)
        assertEquals(department, department2)
    }

    @Test
    fun update_keys(db: Db) {
        val department = Department(5, 10, "PLANNING", "TOKYO", 0)
        db.merge(department, Department::departmentNo)
        assertNull(db.findById<Department>(5))
        assertEquals(department.copy(departmentId = 1), db.findById<Department>(1))
    }

    @Test
    fun update_noKeys(db: Db) {
        val department = Department(1, 50, "PLANNING", "TOKYO", 0)
        db.merge(department)
        val department2 = db.findById<Department>(1)
        assertEquals(department, department2)
    }

    @Test
    fun uniqueConstraintException(db: Db) {
        val department = db.findById<Department>(1)!!
        assertThrows<UniqueConstraintException> { db.merge(department.copy(departmentNo = 20)) }
    }

}
