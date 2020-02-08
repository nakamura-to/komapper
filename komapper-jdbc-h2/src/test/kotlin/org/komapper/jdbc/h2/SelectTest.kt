package org.komapper.jdbc.h2

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.criteria.plus
import org.komapper.core.criteria.select

@ExtendWith(Env::class)
internal class SelectTest(private val db: Db) {

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
        Assertions.assertEquals(
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
        Assertions.assertEquals(
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
        Assertions.assertEquals(
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
        Assertions.assertEquals(
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
        Assertions.assertEquals(
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
        Assertions.assertEquals((10..15).toList(), idList)
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
        Assertions.assertEquals((1..9).toList(), idList)
    }

    @Test
    fun noArg() {
        val list = db.select<Address>()
        Assertions.assertEquals(15, list.size)
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
        Assertions.assertEquals((6..9).toList(), idList)
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
        Assertions.assertEquals(
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
        Assertions.assertEquals(
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
        Assertions.assertEquals(
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
        Assertions.assertEquals((10..15).toList(), idList)
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
        Assertions.assertTrue(list.isEmpty())
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
        Assertions.assertEquals(
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
        Assertions.assertEquals((3..15).toList(), idList)
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
        Assertions.assertTrue(list.isEmpty())
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
        Assertions.assertEquals(
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
        Assertions.assertEquals((3..15).toList(), idList)
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
        Assertions.assertTrue(list.isEmpty())
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
        Assertions.assertEquals((5..10).toList(), idList)
    }

    @Test
    fun isNull() {

        val idList = db.select<Employee> { e ->
            where {
                eq(e[Employee::managerId], null)
            }
        }.map { it.employeeId }
        Assertions.assertEquals(listOf(9), idList)
    }

    @Test
    fun isNotNull() {

        val idList = db.select<Employee> {
            where {
                ne(Employee::managerId, null)
            }
        }.map { it.employeeId }
        Assertions.assertTrue(9 !in idList)
    }

    @Test
    fun sequence() {

        val list = db.select<Address, List<Address>>({
            where {
                ge(Address::addressId, 1)
            }
            orderBy {
                desc(Address::addressId)
            }
            limit(2)
            offset(5)
        }) {
            it.toList()
        }
        Assertions.assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ), list
        )
    }

    @Test
    fun join() {
        val addressMap: MutableMap<Employee, Address> = mutableMapOf()
        val departmentMap: MutableMap<Employee, Department> = mutableMapOf()

        val employees = db.select<Employee> { e ->
            val a = leftJoin<Address> { a ->
                eq(e[Employee::addressId], a[Address::addressId])
                associate { employee, address -> addressMap[employee] = address }
            }
            innerJoin<Department> { d ->
                eq(e[Employee::departmentId], d[Department::departmentId])
                associate { employee, department -> departmentMap[employee] = department }
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
        Assertions.assertEquals(2, employees.size)
        Assertions.assertEquals(2, addressMap.size)
        Assertions.assertEquals(2, departmentMap.size)
        Assertions.assertEquals(listOf(9, 8), employees.map { it.employeeId })
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
        Assertions.assertEquals(
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
        Assertions.assertEquals(6, list.size)
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
        Assertions.assertEquals(6, list.size)
    }
}