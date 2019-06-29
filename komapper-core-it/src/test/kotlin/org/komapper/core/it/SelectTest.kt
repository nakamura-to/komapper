package org.komapper.core.it

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import java.math.BigDecimal

@ExtendWith(Env::class)
class SelectTest {

    @Test
    fun test(db: Db) {
        val list = db.select<Employee> {
            where {
                Employee::salary ge BigDecimal(1000)
            }
            orderBy {
                Employee::employeeId.asc()
            }
            limit { 3 }
            offset { 5 }
        }
        assertEquals(3, list.size)
        assertEquals(listOf(7, 8, 9), list.map { it.employeeId })
    }
}
