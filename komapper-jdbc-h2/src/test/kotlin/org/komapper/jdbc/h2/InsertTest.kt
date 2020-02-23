package org.komapper.jdbc.h2

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.UniqueConstraintException
import org.komapper.core.criteria.insert
import org.komapper.core.entity.EntityDesc
import org.komapper.core.entity.EntityListener
import org.komapper.core.entity.GlobalEntityListener
import org.komapper.core.sql.template

@ExtendWith(Env::class)
internal class InsertTest(private val db: Db) {
    @Test
    fun test() {
        val address = Address(16, "STREET 16", 0)
        db.insert(address)
        val t = template<Address>("select * from address where address_id = 16")
        val address2 = db.select(t).firstOrNull()
        Assertions.assertEquals(address, address2)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    @Test
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preInsert(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postInsert(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }
        })

        val address = Address(16, "STREET 16", 0)
        val address2 = db.insert(address)
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16*",
                0
            ), address2
        )
        val t = template<Address>("select * from address where address_id = 16")
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16",
                0
            ), address3
        )
    }

    @Test
    fun entityListener() {
        val db = Db(
            AddressListenerConfig(
                db.config,
                object :
                    EntityListener<Address> {
                    override fun preInsert(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postInsert(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "${entity.street}*")
                    }
                })
        )

        val address = Address(16, "STREET 16", 0)
        val address2 = db.insert(address)
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16*",
                0
            ), address2
        )
        val t = template<Address>("select * from address where address_id = 16")
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16",
                0
            ), address3
        )
    }

    @Test
    fun createdAt() {
        val person = Person(1, "ABC")
        val newPerson = db.insert(person)
        println(newPerson)
        Assertions.assertTrue(newPerson.createdAt > person.createdAt)
    }

    @Test
    fun uniqueConstraintException() {
        val address = Address(1, "STREET 1", 0)
        assertThrows<UniqueConstraintException> {
            db.insert(
                address
            )
        }
    }

    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val strategy = SequenceStrategy(0, "test")
            val newStrategy = db.insert(strategy)
            Assertions.assertEquals(i, newStrategy.id)
        }
    }

    @Test
    fun multiSequenceGenerator() {
        for (i in 1..201) {
            val strategy = MultiSequenceStrategy(0, 0L)
            val newStrategy = db.insert(strategy)
            Assertions.assertEquals(
                MultiSequenceStrategy(
                    i,
                    i.toLong()
                ), newStrategy
            )
        }
    }

    @Test
    fun embedded() {
        val employee = Employee(
            employeeId = 100,
            employeeNo = 9999,
            employeeName = "aaa",
            managerId = null,
            detail = EmployeeDetail(
                LocalDate.of(2019, 6, 15),
                BigDecimal("2000.00")
            ),
            departmentId = 1,
            addressId = 1,
            version = 1
        )
        db.insert(employee)
        val employee2 = db.findById<Employee>(100)
        Assertions.assertEquals(employee, employee2)
    }

    @Test
    fun nestedEmbedded() {
        val salary =
            WorkerSalary(BigDecimal("2000.00"))
        val worker = Worker(
            employeeId = 100,
            employeeNo = 9999,
            employeeName = "aaa",
            managerId = null,
            detail = WorkerDetail(
                LocalDate.of(
                    2019,
                    6,
                    15
                ), salary
            ),
            departmentId = 1,
            addressId = 1,
            version = 1
        )
        db.insert(worker)
        val worker2 = db.findById<Worker>(100)
        Assertions.assertEquals(worker, worker2)
    }

    @Test
    fun embedded_valueAssignment() {
        val human = Human(
            name = "aaa",
            common = Common()
        )
        val human2 = db.insert(human)
        val human3 = db.findById<Human>(1, 0)
        Assertions.assertEquals(human2, human3)
    }

    @Test
    fun criteria() {
        val query = insert<Address> {
            values {
                value(Address::addressId, 16)
                value(Address::street, "new street")
                value(Address::version, 1)
            }
        }
        val count = db.insert(query)
        Assertions.assertEquals(1, count)
        val address = db.findById<Address>(16)
        Assertions.assertEquals(Address(16, "new street", 1), address)
    }
}
