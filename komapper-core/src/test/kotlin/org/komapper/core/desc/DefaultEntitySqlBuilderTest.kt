package org.komapper.core.desc

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.komapper.core.DeleteOption
import org.komapper.core.InsertOption
import org.komapper.core.UpdateOption
import org.komapper.core.metadata.CollectedMetadataResolver
import org.komapper.core.metadata.entities

internal class DefaultEntitySqlBuilderTest {

    private val metadataResolver = CollectedMetadataResolver(
        entities {
            entity(Employee::class) {
                id(Employee::employeeId)
                embedded(Employee::detail)
                version(Employee::version)
            }
        }
    )

    private val namingStrategy = CamelToSnake()

    private val dataDescFactory = DefaultDataDescFactory(
        metadataResolver,
        DefaultPropDescFactory(
            { it },
            namingStrategy
        )
    )

    private val factory = DefaultEntityDescFactory(
        dataDescFactory,
        { it },
        namingStrategy
    )

    val builder = DefaultEntitySqlBuilder { value, _ -> value.toString() }

    data class EmployeeDetail(
        val hiredate: LocalDate,
        val salary: BigDecimal
    )

    data class Employee(
        val employeeId: Int = 0,
        val employeeNo: Int = 10,
        val employeeName: String = "a",
        val detail: EmployeeDetail = EmployeeDetail(
            hiredate = LocalDate.of(2019, 7, 6),
            salary = BigDecimal(2000.00)
        ),
        val version: Int = 1
    )

    @Nested
    inner class Insert {
        @Test
        fun test() {
            val desc = factory.get(Employee::class)
            val sql = builder.buildInsert(desc, Employee(), InsertOption())
            assertEquals(
                "insert into employee (employee_id, employee_no, employee_name, hiredate, salary, version) " +
                        "values (?, ?, ?, ?, ?, ?)", sql.text
            )
        }

        @Test
        fun include() {
            val desc = factory.get(Employee::class)
            val sql = builder.buildInsert(
                desc,
                Employee(),
                InsertOption(include = listOf(Employee::employeeName, EmployeeDetail::salary))
            )
            assertEquals(
                "insert into employee (employee_name, salary) values (?, ?)", sql.text
            )
        }

        @Test
        fun exclude() {
            val desc = factory.get(Employee::class)
            val sql = builder.buildInsert(
                desc,
                Employee(),
                InsertOption(exclude = listOf(Employee::employeeName, EmployeeDetail::salary))
            )
            assertEquals(
                "insert into employee (employee_id, employee_no, hiredate, version) values (?, ?, ?, ?)", sql.text
            )
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun test() {
            val meta = factory.get(Employee::class)
            val sql = builder.buildDelete(meta, Employee(), DeleteOption())
            assertEquals(
                "delete from employee where employee_id = ? and version = ?", sql.text
            )
        }

        @Test
        fun ignoreVersion() {
            val meta = factory.get(Employee::class)
            val sql = builder.buildDelete(meta, Employee(), DeleteOption(ignoreVersion = true))
            assertEquals(
                "delete from employee where employee_id = ?", sql.text
            )
        }
    }

    @Nested
    inner class Update {
        @Test
        fun test() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpdate(desc, entity, newEntity, UpdateOption())
            assertEquals(
                "update employee set employee_no = ?, employee_name = ?, hiredate = ?, salary = ?, " +
                        "version = ? where employee_id = ? and version = ?", sql.text
            )
        }

        @Test
        fun ignoreVersion() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpdate(desc, entity, newEntity, UpdateOption(ignoreVersion = true))
            assertEquals(
                "update employee set employee_no = ?, employee_name = ?, hiredate = ?, salary = ?, " +
                        "version = ? where employee_id = ?", sql.text
            )
        }

        @Test
        fun include() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpdate(
                desc,
                entity,
                newEntity,
                UpdateOption(include = listOf(Employee::employeeName, EmployeeDetail::salary))
            )
            assertEquals(
                "update employee set employee_name = ?, salary = ? " +
                        "where employee_id = ? and version = ?", sql.text
            )
        }

        @Test
        fun exclude() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpdate(
                desc,
                entity,
                newEntity,
                UpdateOption(exclude = listOf(Employee::employeeName, EmployeeDetail::salary))
            )
            assertEquals(
                "update employee set employee_no = ?, hiredate = ?, version = ? " +
                        "where employee_id = ? and version = ?", sql.text
            )
        }
    }

    @Nested
    inner class Upsert {
        @Test
        fun test() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpsert(desc, entity, newEntity, emptyList(), InsertOption(), UpdateOption())
            assertEquals(
                "insert into employee as t_ " +
                        "(employee_id, employee_no, employee_name, hiredate, salary, version) " +
                        "values(?, ?, ?, ?, ?, ?) on conflict (employee_id) " +
                        "do update set employee_no = ?, employee_name = ?, hiredate = ?, salary = ?, version = ? " +
                        "where version = ?", sql.text
            )
        }

        @Test
        fun insert_include() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpsert(
                desc,
                entity,
                newEntity,
                emptyList(),
                InsertOption(include = listOf(Employee::employeeName, EmployeeDetail::salary)),
                UpdateOption()
            )
            assertEquals(
                "insert into employee as t_ " +
                        "(employee_name, salary) " +
                        "values(?, ?) on conflict (employee_id) " +
                        "do update set employee_no = ?, employee_name = ?, hiredate = ?, salary = ?, version = ? " +
                        "where version = ?", sql.text
            )
        }

        @Test
        fun insert_exclude() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpsert(
                desc,
                entity,
                newEntity,
                emptyList(),
                InsertOption(exclude = listOf(Employee::employeeName, EmployeeDetail::salary)),
                UpdateOption()
            )
            assertEquals(
                "insert into employee as t_ " +
                        "(employee_id, employee_no, hiredate, version) " +
                        "values(?, ?, ?, ?) on conflict (employee_id) " +
                        "do update set employee_no = ?, employee_name = ?, hiredate = ?, salary = ?, version = ? " +
                        "where version = ?", sql.text
            )
        }

        @Test
        fun update_ignoreVersion() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpsert(
                desc,
                entity,
                newEntity,
                emptyList(),
                InsertOption(),
                UpdateOption(ignoreVersion = true)
            )
            assertEquals(
                "insert into employee as t_ " +
                        "(employee_id, employee_no, employee_name, hiredate, salary, version) " +
                        "values(?, ?, ?, ?, ?, ?) on conflict (employee_id) " +
                        "do update set employee_no = ?, employee_name = ?, hiredate = ?, salary = ?, version = ?",
                sql.text
            )
        }

        @Test
        fun update_include() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpsert(
                desc,
                entity,
                newEntity,
                emptyList(),
                InsertOption(),
                UpdateOption(include = listOf(Employee::employeeName, EmployeeDetail::salary))
            )
            assertEquals(
                "insert into employee as t_ " +
                        "(employee_id, employee_no, employee_name, hiredate, salary, version) " +
                        "values(?, ?, ?, ?, ?, ?) on conflict (employee_id) " +
                        "do update set employee_name = ?, salary = ? " +
                        "where version = ?", sql.text
            )
        }

        @Test
        fun update_exclude() {
            val desc = factory.get(Employee::class)
            val entity = Employee()
            val newEntity = entity.copy(version = entity.version.inc())
            val sql = builder.buildUpsert(
                desc,
                entity,
                newEntity,
                emptyList(),
                InsertOption(),
                UpdateOption(exclude = listOf(Employee::employeeName, EmployeeDetail::salary))
            )
            assertEquals(
                "insert into employee as t_ " +
                        "(employee_id, employee_no, employee_name, hiredate, salary, version) " +
                        "values(?, ?, ?, ?, ?, ?) on conflict (employee_id) " +
                        "do update set employee_no = ?, hiredate = ?, version = ? " +
                        "where version = ?", sql.text
            )
        }
    }
}
