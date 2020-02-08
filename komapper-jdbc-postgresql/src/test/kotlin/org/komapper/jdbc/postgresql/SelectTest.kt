package org.komapper.jdbc.postgresql

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db

@ExtendWith(Env::class)
class SelectTest {

    @Test
    fun test(db: Db) {
        val list = db.select<Employee> {
            where {
                ge(Employee::salary, BigDecimal(1000))
            }
            orderBy {
                asc(Employee::employeeId)
            }
            limit(3)
            offset(5)
        }
        assertEquals(3, list.size)
        assertEquals(listOf(7, 8, 9), list.map { it.employeeId })
    }

    @Test
    fun join(db: Db) {
        val addressMap: MutableMap<Employee, Address> = mutableMapOf()
        val departmentMap: MutableMap<Employee, Department> = mutableMapOf()
        val employees = db.select<Employee> { e ->
            leftJoin<Address> { a ->
                eq(e[Employee::addressId], a[Address::addressId])
                associate { employee, address ->
                    addressMap[employee] = address
                }
            }
            innerJoin<Department> { d ->
                eq(e[Employee::departmentId], d[Department::departmentId])
                associate { employee, department ->
                    departmentMap[employee] = department
                }
            }
            where {
                ge(Employee::addressId, 1)
            }
            orderBy {
                desc(Employee::addressId)
            }
            limit(2)
            offset(5)
        }
        assertEquals(2, employees.size)
        assertEquals(2, addressMap.size)
        assertEquals(2, departmentMap.size)
        assertEquals(listOf(9, 8), employees.map { it.employeeId })
    }

    @Test
    fun in2(db: Db) {
        val list = db.select<Employee> {
            where {
                in2(Employee::managerId, Employee::departmentId, listOf(6 to 3))
            }
            orderBy {
                asc(Employee::employeeId)
            }
        }
        assertEquals(5, list.size)
    }
}
