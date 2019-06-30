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

    @Test
    fun join(db: Db) {
        val addressMap: MutableMap<Employee, Address> = mutableMapOf()
        val departmentMap: MutableMap<Employee, Department> = mutableMapOf()
        val employees = db.select<Employee> {
            leftJoin<Address>({ Employee::addressId eq Address::addressId }) { employee, address ->
                addressMap[employee] = address
            }
            innerJoin<Department>({ Employee::departmentId eq Department::departmentId }) { employee, department ->
                departmentMap[employee] = department
            }
            where {
                Address::addressId ge 1
            }
            orderBy {
                Address::addressId.desc()
            }
            limit { 2 }
            offset { 5 }
        }
        assertEquals(2, employees.size)
        assertEquals(2, addressMap.size)
        assertEquals(2, departmentMap.size)
        assertEquals(listOf(9, 8), employees.map { it.employeeId })
    }

}
