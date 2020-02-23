package org.komapper.jdbc.h2

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.criteria.plus
import org.komapper.core.criteria.select

@ExtendWith(Env::class)
internal class SelectByCriteriaTest(private val db: Db) {

    @Test
    fun test() {
        val list = db.select<Address> {
            where {
                ge(Address::addressId, 1)
            }
            orderBy {
                desc(Address::addressId)
            }
            limit(2)
            offset(5)
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun passCriteriaQuery() {
        val criteriaQuery =
            select<Address> {
                where {
                    ge(Address::addressId, 1)
                }
                orderBy {
                    desc(Address::addressId)
                }
                limit(2)
                offset(5)
            }
        val list = db.select(criteriaQuery)
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun invokeCriteriaQuery() {
        val criteriaQuery =
            select<Address> {
                where {
                    ge(Address::addressId, 1)
                }
                orderBy {
                    desc(Address::addressId)
                }
                limit(2)
                offset(5)
            }
        val list = db.select<Address> { alias ->
            criteriaQuery(alias)
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun composeCriteriaQuery() {
        val query1 =
            select<Address> {
                where {
                    ge(Address::addressId, 1)
                }
            }
        val query2 =
            select<Address> {
                orderBy {
                    desc(Address::addressId)
                }
                limit(2)
                offset(5)
            }
        val query3 = query1 + query2
        val list = db.select(query3)
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun distinct() {
        val list = db.select<NoId> {
            distinct()
        }
        assertEquals(
            listOf(
                NoId(1, 1)
            ), list
        )
    }

    @Test
    fun like() {
        val idList = db.select<Address> {
            where {
                like(Address::street, "STREET 1_")
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((10..15).toList(), idList)
    }

    @Test
    fun notLike() {
        val idList = db.select<Address> {
            where {
                notLike(Address::street, "STREET 1_")
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((1..9).toList(), idList)
    }

    @Test
    fun noArg() {
        val list = db.select<Address>()
        assertEquals(15, list.size)
    }

    @Test
    fun not() {
        val idList = db.select<Address> {
            where {
                gt(Address::addressId, 5)
                not {
                    ge(Address::addressId, 10)
                }
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((6..9).toList(), idList)
    }

    @Test
    fun and() {

        val list = db.select<Address> {
            where {
                ge(Address::addressId, 1)
                and {
                    ge(Address::addressId, 1)
                }
            }
            orderBy {
                desc(Address::addressId)
            }
            limit(2)
            offset(5)
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun or() {

        val list = db.select<Address> {
            where {
                ge(Address::addressId, 1)
                or {
                    ge(Address::addressId, 1)
                    ge(Address::addressId, 1)
                }
            }
            orderBy {
                desc(Address::addressId)
            }
            limit(2)
            offset(5)
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun `in`() {

        val list = db.select<Address> {
            where {
                `in`(Address::addressId, listOf(9, 10))
            }
            orderBy {
                desc(Address::addressId)
            }
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun notIn() {

        val idList = db.select<Address> {
            where {
                notIn(Address::addressId, (1..9).toList())
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((10..15).toList(), idList)
    }

    @Test
    fun in_empty() {

        val list = db.select<Address> {
            where {
                `in`(Address::addressId, emptyList())
            }
            orderBy {
                desc(Address::addressId)
            }
        }
        assertTrue(list.isEmpty())
    }

    @Test
    fun in2() {

        val list = db.select<Address> {
            where {
                in2(Address::addressId, Address::street, listOf(9 to "STREET 9", 10 to "STREET 10"))
            }
            orderBy {
                desc(Address::addressId)
            }
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun notIn2() {

        val idList = db.select<Address> {
            where {
                notIn2(Address::addressId, Address::street, listOf(1 to "STREET 1", 2 to "STREET 2"))
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((3..15).toList(), idList)
    }

    @Test
    fun in2_empty() {

        val list = db.select<Address> {
            where {
                in2(Address::addressId, Address::street, emptyList())
            }
            orderBy {
                desc(Address::addressId)
            }
        }
        assertTrue(list.isEmpty())
    }

    @Test
    fun in3() {

        val list = db.select<Address> {
            where {
                in3(
                    Address::addressId,
                    Address::street,
                    Address::version,
                    listOf(
                        Triple(9, "STREET 9", 1),
                        Triple(10, "STREET 10", 1)
                    )
                )
            }
            orderBy {
                desc(Address::addressId)
            }
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun notIn3() {

        val idList = db.select<Address> {
            where {
                notIn3(
                    Address::addressId, Address::street, Address::version,
                    listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                )
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((3..15).toList(), idList)
    }

    @Test
    fun in3_empty() {

        val list = db.select<Address> {
            where {
                in3(Address::addressId, Address::street, Address::version, emptyList())
            }
            orderBy {
                desc(Address::addressId)
            }
        }
        assertTrue(list.isEmpty())
    }

    @Test
    fun between() {

        val idList = db.select<Address> {
            where {
                between(Address::addressId, 5, 10)
            }
            orderBy {
                asc(Address::addressId)
            }
        }.map { it.addressId }
        assertEquals((5..10).toList(), idList)
    }

    @Test
    fun isNull() {

        val idList = db.select<Employee> { e ->
            where {
                eq(e[Employee::managerId], null)
            }
        }.map { it.employeeId }
        assertEquals(listOf(9), idList)
    }

    @Test
    fun isNotNull() {

        val idList = db.select<Employee> {
            where {
                ne(Employee::managerId, null)
            }
        }.map { it.employeeId }
        assertTrue(9 !in idList)
    }

    @Test
    fun join() {
        val addressMap = mutableMapOf<Employee, List<Address>>()
        val departmentMap = mutableMapOf<Employee, List<Department>>()

        val employees = db.select<Employee> { e ->
            val a = leftJoin<Address> { a ->
                eq(e[Employee::addressId], a[Address::addressId])
                associate { employee, addresses -> addressMap[employee] = addresses }
            }
            innerJoin<Department> { d ->
                eq(e[Employee::departmentId], d[Department::departmentId])
                associate { employee, departments -> departmentMap[employee] = departments }
            }
            where {
                ge(a[Address::addressId], 1)
            }
            orderBy {
                desc(a[Address::addressId])
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
    fun forUpdate() {

        val list = db.select<Address> {
            where {
                ge(Address::addressId, 1)
            }
            orderBy {
                desc(Address::addressId)
            }
            limit(2)
            offset(5)
            forUpdate {
                nowait()
            }
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun embedded() {

        val list = db.select<Employee> {
            where {
                ge(
                    EmployeeDetail::salary,
                    BigDecimal(
                        "2000.00"
                    )
                )
            }
        }
        assertEquals(6, list.size)
    }

    @Test
    fun nestedEmbedded() {

        val list = db.select<Worker> {
            where {
                ge(
                    WorkerSalary::salary,
                    BigDecimal(
                        "2000.00"
                    )
                )
            }
        }
        assertEquals(6, list.size)
    }

    @Test
    fun exists() {
        val criteriaQuery =
            select<Employee> { e ->
                where {
                    exists<Address> { a ->
                        where {
                            eq(e[Employee::addressId], a[Address::addressId])
                            like(e[Employee::employeeName], "%S%")
                        }
                    }
                }
            }
        val list = db.select(criteriaQuery)
        assertEquals(5, list.size)
    }

    @Test
    fun notExists() {
        val criteriaQuery =
            select<Employee> { e ->
                where {
                    notExists<Address> { a ->
                        where {
                            eq(e[Employee::addressId], a[Address::addressId])
                            like(e[Employee::employeeName], "%S%")
                        }
                    }
                }
            }
        val list = db.select(criteriaQuery)
        assertEquals(9, list.size)
    }
}
