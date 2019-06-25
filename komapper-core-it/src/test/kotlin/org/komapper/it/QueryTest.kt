package org.komapper.it

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import java.math.BigDecimal

@ExtendWith(Env::class)
class QueryTest {

    @Test
    fun test(db: Db) {
        db.transaction {
            val list = db.query<Employee> {
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
}
