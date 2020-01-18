package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db

@ExtendWith(Env::class)
internal class QueryTest(private val db: Db) {

    @Test
    fun test() {

        val list = db.query<Address>("select * from address")
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
    }

    @Test
    fun expand() {

        val list = db.query<Address>("select /*%expand*/* from address")
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
    }

    @Test
    fun sequence() {

        val list = db.query<Address, List<Address>>("select * from address") {
            it.toList()
        }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
    }

    @Test
    fun sequence_expand() {

        val list = db.query<Address, List<Address>>("select /*%expand*/* from address") {
            it.toList()
        }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
    }

    @Test
    fun condition_objectExpression() {

        val list =
            db.query<Address>(
                "select * from address where street = /*street*/'test'", object {
                    val street = "STREET 10"
                }
            )
        Assertions.assertEquals(1, list.size)
        Assertions.assertEquals(
            Address(
                10,
                "STREET 10",
                1
            ), list[0]
        )
    }

    @Test
    fun condition_dataClass() {
        data class Condition(val street: String)

        val list =
            db.query<Address>(
                "select * from address where street = /*street*/'test'", Condition("STREET 10")
            )
        Assertions.assertEquals(1, list.size)
        Assertions.assertEquals(
            Address(
                10,
                "STREET 10",
                1
            ), list[0]
        )
    }

    @Test
    fun embedded() {

        val list =
            db.query<Employee>(
                """
            select employee_id, employee_no, employee_name, manager_id,
            hiredate, salary, department_id, address_id, version from employee
        """.trimIndent()
            )
        Assertions.assertEquals(14, list.size)
    }

    @Test
    fun nestedEmbedded() {

        val list =
            db.query<Worker>(
                """
            select employee_id, employee_no, employee_name, manager_id,
            hiredate, salary, department_id, address_id, version from employee
            """.trimIndent()
            )
        Assertions.assertEquals(14, list.size)
    }

    @Test
    fun `in`() {

        val list = db.query<Address>(
            "select * from address where address_id in /*list*/(0)",
            object {
                val list = listOf(1, 2)
            }
        )
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
        Assertions.assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ), list[1]
        )
    }

    @Test
    fun in2() {

        val list = db.query<Address>(
            "select * from address where (address_id, street) in /*pairs*/(0, '')",
            object {
                val pairs = listOf(1 to "STREET 1", 2 to "STREET 2")
            }
        )
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
        Assertions.assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ), list[1]
        )
    }

    @Test
    fun in3() {

        val list = db.query<Address>(
            "select * from address where (address_id, street, version) in /*triples*/(0, '', 0)",
            object {
                val triples = listOf(Triple(1, "STREET 1", 1), Triple(2, "STREET 2", 1))
            }
        )
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(
            Address(
                1,
                "STREET 1",
                1
            ), list[0]
        )
        Assertions.assertEquals(
            Address(
                2,
                "STREET 2",
                1
            ), list[1]
        )
    }
}
