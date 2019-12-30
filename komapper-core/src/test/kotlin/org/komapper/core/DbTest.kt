package org.komapper.core

import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.criteria.OrderByScope
import org.komapper.core.criteria.WhereScope
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityListener
import org.komapper.core.jdbc.H2Dialect
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.logging.StdoutLogger
import org.komapper.core.metadata.EntityMetadata
import org.komapper.core.metadata.SequenceGenerator
import org.komapper.core.sql.Sql

@Suppress("UNUSED")
internal class DbTest {

    data class Address(
        val addressId: Int,
        val street: String,
        val version: Int
    )

    object AddressMetadata : EntityMetadata<Address>({
        id(Address::addressId)
        version(Address::version)
    })

    data class CompositeKeyAddress(
        val addressId1: Int,
        val addressId2: Int,
        val street: String,
        val version: Int
    )

    object CompositeKeyAddressMetadata : EntityMetadata<CompositeKeyAddress>({
        id(CompositeKeyAddress::addressId1)
        id(CompositeKeyAddress::addressId2)
        version(CompositeKeyAddress::version)
        table {
            name("COMP_KEY_ADDRESS")
        }
    })

    data class SequenceStrategy(
        val id: Int,
        val value: String
    )

    object SequenceStrategyMetadata : EntityMetadata<SequenceStrategy>({
        id(SequenceStrategy::id, SequenceGenerator("SEQUENCE_STRATEGY_ID", 100))
        table {
            name("SEQUENCE_STRATEGY")
        }
    })

    data class MultiSequenceStrategy(
        val id: Int,
        val value: Long
    )

    object MultiSequenceStrategyMetadata : EntityMetadata<MultiSequenceStrategy>({
        id(MultiSequenceStrategy::id, SequenceGenerator("SEQUENCE_STRATEGY_ID", 100))
        id(MultiSequenceStrategy::value, SequenceGenerator("MY_SEQUENCE_STRATEGY_ID", 100))

        table {
            name("SEQUENCE_STRATEGY")
        }
    })

    data class Quotes(
        val id: Int,
        val value: String
    )

    object QuotesMetadata : EntityMetadata<Quotes>({
        id(Quotes::id, SequenceGenerator("SEQUENCE_STRATEGY_ID", quote = true))
        table {
            name(name = "SEQUENCE_STRATEGY", quote = true)
            column(Quotes::id, "ID", quote = true)
            column(Quotes::value, "VALUE", quote = true)
        }
    })

    data class Person(
        val personId: Int,
        val name: String,
        val createdAt: LocalDateTime = LocalDateTime.MIN,
        val updatedAt: LocalDateTime = LocalDateTime.MIN
    )

    object PersonMetadata : EntityMetadata<Person>({
        id(Person::personId)
        createdAt(Person::createdAt)
        updatedAt(Person::updatedAt)
    })

    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    data class EmployeeDetail(
        val hiredate: LocalDate,
        val salary: BigDecimal
    )

    data class Employee(
        val employeeId: Int,
        val employeeNo: Int,
        val employeeName: String,
        val managerId: Int?,
        val detail: EmployeeDetail,
        val departmentId: Int,
        val addressId: Int,
        val version: Int
    )

    object EmployeeMetadata : EntityMetadata<Employee>({
        id(Employee::employeeId)
        embedded(Employee::detail)
        version(Employee::version)
    })

    data class WorkerSalary(val salary: BigDecimal)

    data class WorkerDetail(
        val hiredate: LocalDate,
        val salary: WorkerSalary
    )

    object WorkerDetailMetadata : EntityMetadata<WorkerDetail>({
        embedded(WorkerDetail::salary)
    })

    data class Worker(
        val employeeId: Int,
        val employeeNo: Int,
        val employeeName: String,
        val managerId: Int?,
        val detail: WorkerDetail,
        val departmentId: Int,
        val addressId: Int,
        val version: Int
    )

    object WorkerMetadata : EntityMetadata<Worker>({
        id(Worker::employeeId)
        embedded(Worker::detail)
        version(Worker::version)
        table {
            name("employee")
        }
    })

    data class Common(
        val personId: Int = 0,
        val createdAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0),
        val updatedAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0),
        val version: Int = 0
    )

    class CommonMetadata : EntityMetadata<Common>({
        id(Common::personId, SequenceGenerator("PERSON_ID_SEQUENCE", 100))
        createdAt(Common::createdAt)
        updatedAt(Common::updatedAt)
        version(Common::version)
    })

    data class Human(
        val name: String,
        val common: Common
    )

    object HumanMetadata : EntityMetadata<Human>({
        embedded(Human::common)
        table {
            name("person")
        }
    })

    data class Department(
        val departmentId: Int,
        val departmentNo: Int,
        val departmentName: String,
        val Location: String,
        val version: Int
    )

    object DepartmentMetadata : EntityMetadata<Department>({
        id(Department::departmentId)
        version(Department::version)
    })

    private val config = DbConfig(
        dataSource = SimpleDataSource("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
        dialect = H2Dialect(),
        batchSize = 2
    )

    @BeforeEach
    fun before() {
        config.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE SEQUENCE SEQUENCE_STRATEGY_ID START WITH 1 INCREMENT BY 100;
                    CREATE SEQUENCE MY_SEQUENCE_STRATEGY_ID START WITH 1 INCREMENT BY 100;
                    CREATE SEQUENCE PERSON_ID_SEQUENCE START WITH 1 INCREMENT BY 100;

                    CREATE TABLE DEPARTMENT(DEPARTMENT_ID INTEGER NOT NULL PRIMARY KEY, DEPARTMENT_NO INTEGER NOT NULL UNIQUE,DEPARTMENT_NAME VARCHAR(20),LOCATION VARCHAR(20) DEFAULT 'TOKYO', VERSION INTEGER);
                    CREATE TABLE ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
                    CREATE TABLE EMPLOYEE(EMPLOYEE_ID INTEGER NOT NULL PRIMARY KEY, EMPLOYEE_NO INTEGER NOT NULL ,EMPLOYEE_NAME VARCHAR(20),MANAGER_ID INTEGER,HIREDATE DATE,SALARY NUMERIC(7,2),DEPARTMENT_ID INTEGER,ADDRESS_ID INTEGER,VERSION INTEGER, CONSTRAINT FK_DEPARTMENT_ID FOREIGN KEY(DEPARTMENT_ID) REFERENCES DEPARTMENT(DEPARTMENT_ID), CONSTRAINT FK_ADDRESS_ID FOREIGN KEY(ADDRESS_ID) REFERENCES ADDRESS(ADDRESS_ID));
                    CREATE TABLE PERSON(PERSON_ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(20), CREATED_AT TIMESTAMP, UPDATED_AT TIMESTAMP, VERSION INTEGER);

                    CREATE TABLE COMP_KEY_DEPARTMENT(DEPARTMENT_ID1 INTEGER NOT NULL, DEPARTMENT_ID2 INTEGER NOT NULL, DEPARTMENT_NO INTEGER NOT NULL UNIQUE,DEPARTMENT_NAME VARCHAR(20),LOCATION VARCHAR(20) DEFAULT 'TOKYO', VERSION INTEGER, CONSTRAINT PK_COMP_KEY_DEPARTMENT PRIMARY KEY(DEPARTMENT_ID1, DEPARTMENT_ID2));
                    CREATE TABLE COMP_KEY_ADDRESS(ADDRESS_ID1 INTEGER NOT NULL, ADDRESS_ID2 INTEGER NOT NULL, STREET VARCHAR(20), VERSION INTEGER, CONSTRAINT PK_COMP_KEY_ADDRESS PRIMARY KEY(ADDRESS_ID1, ADDRESS_ID2));
                    CREATE TABLE COMP_KEY_EMPLOYEE(EMPLOYEE_ID1 INTEGER NOT NULL, EMPLOYEE_ID2 INTEGER NOT NULL, EMPLOYEE_NO INTEGER NOT NULL ,EMPLOYEE_NAME VARCHAR(20),MANAGER_ID1 INTEGER,MANAGER_ID2 INTEGER,HIREDATE DATE,SALARY NUMERIC(7,2),DEPARTMENT_ID1 INTEGER,DEPARTMENT_ID2 INTEGER,ADDRESS_ID1 INTEGER,ADDRESS_ID2 INTEGER,VERSION INTEGER, CONSTRAINT PK_COMP_KEY_EMPLOYEE PRIMARY KEY(EMPLOYEE_ID1, EMPLOYEE_ID2), CONSTRAINT FK_COMP_KEY_DEPARTMENT_ID FOREIGN KEY(DEPARTMENT_ID1, DEPARTMENT_ID2) REFERENCES COMP_KEY_DEPARTMENT(DEPARTMENT_ID1, DEPARTMENT_ID2), CONSTRAINT FK_COMP_KEY_ADDRESS_ID FOREIGN KEY(ADDRESS_ID1, ADDRESS_ID2) REFERENCES COMP_KEY_ADDRESS(ADDRESS_ID1, ADDRESS_ID2));

                    CREATE TABLE LARGE_OBJECT(ID NUMERIC(8) NOT NULL PRIMARY KEY, NAME VARCHAR(20), LARGE_NAME CLOB, BYTES BINARY, LARGE_BYTES BLOB, DTO BINARY, LARGE_DTO BLOB);
                    CREATE TABLE TENSE (ID INTEGER PRIMARY KEY,DATE_DATE DATE, DATE_TIME TIME, DATE_TIMESTAMP TIMESTAMP, CAL_DATE DATE, CAL_TIME TIME, CAL_TIMESTAMP TIMESTAMP, SQL_DATE DATE, SQL_TIME TIME, SQL_TIMESTAMP TIMESTAMP);
                    CREATE TABLE JOB (ID INTEGER NOT NULL PRIMARY KEY, JOB_TYPE VARCHAR(20));
                    CREATE TABLE AUTHORITY (ID INTEGER NOT NULL PRIMARY KEY, AUTHORITY_TYPE INTEGER);
                    CREATE TABLE NO_ID (VALUE1 INTEGER, VALUE2 INTEGER);
                    CREATE TABLE OWNER_OF_NO_ID (ID INTEGER NOT NULL PRIMARY KEY, NO_ID_VALUE1 INTEGER);
                    CREATE TABLE CONSTRAINT_CHECKING (PRIMARY_KEY INTEGER PRIMARY KEY, UNIQUE_KEY INTEGER UNIQUE, FOREIGN_KEY INTEGER, CHECK_CONSTRAINT INTEGER, NOT_NULL INTEGER NOT NULL, CONSTRAINT CK_CONSTRAINT_CHECKING_1 CHECK (CHECK_CONSTRAINT > 0), CONSTRAINT FK_JOB_ID FOREIGN KEY (FOREIGN_KEY) REFERENCES JOB (ID));
                    CREATE TABLE PATTERN (VALUE VARCHAR(10));

                    CREATE TABLE ID_GENERATOR(PK VARCHAR(20) NOT NULL PRIMARY KEY, VALUE INTEGER NOT NULL);
                    CREATE TABLE MY_ID_GENERATOR(MY_PK VARCHAR(20) NOT NULL PRIMARY KEY, MY_VALUE INTEGER NOT NULL);
                    CREATE TABLE AUTO_STRATEGY(ID INTEGER NOT NULL IDENTITY PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE IDENTITY_STRATEGY(ID INTEGER NOT NULL IDENTITY PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE SEQUENCE_STRATEGY(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE SEQUENCE_STRATEGY2(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE TABLE_STRATEGY(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE TABLE_STRATEGY2(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));

                    CREATE TABLE ANY_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE OTHER);
                    CREATE TABLE BIG_DECIMAL_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BIGINT);
                    CREATE TABLE BIG_INTEGER_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BIGINT);
                    CREATE TABLE BOOLEAN_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BOOL);
                    CREATE TABLE BYTE_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TINYINT);
                    CREATE TABLE BYTE_ARRAY_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BINARY);
                    CREATE TABLE DOUBLE_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE DOUBLE);
                    CREATE TABLE ENUM_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(20));
                    CREATE TABLE FLOAT_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE FLOAT);
                    CREATE TABLE INT_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE INTEGER);
                    CREATE TABLE LOCAL_DATE_TIME_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TIMESTAMP);
                    CREATE TABLE LOCAL_DATE_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE DATE);
                    CREATE TABLE LOCAL_TIME_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TIME);
                    CREATE TABLE LONG_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BIGINT);
                    CREATE TABLE OFFSET_DATE_TIME_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE TIMESTAMP WITH TIME ZONE);
                    CREATE TABLE SHORT_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE SMALLINT);
                    CREATE TABLE STRING_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(20));

                    INSERT INTO DEPARTMENT VALUES(1,10,'ACCOUNTING','NEW YORK',1);
                    INSERT INTO DEPARTMENT VALUES(2,20,'RESEARCH','DALLAS',1);
                    INSERT INTO DEPARTMENT VALUES(3,30,'SALES','CHICAGO',1);
                    INSERT INTO DEPARTMENT VALUES(4,40,'OPERATIONS','BOSTON',1);
                    INSERT INTO ADDRESS VALUES(1,'STREET 1',1);
                    INSERT INTO ADDRESS VALUES(2,'STREET 2',1);
                    INSERT INTO ADDRESS VALUES(3,'STREET 3',1);
                    INSERT INTO ADDRESS VALUES(4,'STREET 4',1);
                    INSERT INTO ADDRESS VALUES(5,'STREET 5',1);
                    INSERT INTO ADDRESS VALUES(6,'STREET 6',1);
                    INSERT INTO ADDRESS VALUES(7,'STREET 7',1);
                    INSERT INTO ADDRESS VALUES(8,'STREET 8',1);
                    INSERT INTO ADDRESS VALUES(9,'STREET 9',1);
                    INSERT INTO ADDRESS VALUES(10,'STREET 10',1);
                    INSERT INTO ADDRESS VALUES(11,'STREET 11',1);
                    INSERT INTO ADDRESS VALUES(12,'STREET 12',1);
                    INSERT INTO ADDRESS VALUES(13,'STREET 13',1);
                    INSERT INTO ADDRESS VALUES(14,'STREET 14',1);
                    INSERT INTO ADDRESS VALUES(15,'STREET 15',1);
                    INSERT INTO EMPLOYEE VALUES(1,7369,'SMITH',13,'1980-12-17',800,2,1,1);
                    INSERT INTO EMPLOYEE VALUES(2,7499,'ALLEN',6,'1981-02-20',1600,3,2,1);
                    INSERT INTO EMPLOYEE VALUES(3,7521,'WARD',6,'1981-02-22',1250,3,3,1);
                    INSERT INTO EMPLOYEE VALUES(4,7566,'JONES',9,'1981-04-02',2975,2,4,1);
                    INSERT INTO EMPLOYEE VALUES(5,7654,'MARTIN',6,'1981-09-28',1250,3,5,1);
                    INSERT INTO EMPLOYEE VALUES(6,7698,'BLAKE',9,'1981-05-01',2850,3,6,1);
                    INSERT INTO EMPLOYEE VALUES(7,7782,'CLARK',9,'1981-06-09',2450,1,7,1);
                    INSERT INTO EMPLOYEE VALUES(8,7788,'SCOTT',4,'1982-12-09',3000.0,2,8,1);
                    INSERT INTO EMPLOYEE VALUES(9,7839,'KING',NULL,'1981-11-17',5000,1,9,1);
                    INSERT INTO EMPLOYEE VALUES(10,7844,'TURNER',6,'1981-09-08',1500,3,10,1);
                    INSERT INTO EMPLOYEE VALUES(11,7876,'ADAMS',8,'1983-01-12',1100,2,11,1);
                    INSERT INTO EMPLOYEE VALUES(12,7900,'JAMES',6,'1981-12-03',950,3,12,1);
                    INSERT INTO EMPLOYEE VALUES(13,7902,'FORD',4,'1981-12-03',3000,2,13,1);
                    INSERT INTO EMPLOYEE VALUES(14,7934,'MILLER',7,'1982-01-23',1300,1,14,1);

                    INSERT INTO COMP_KEY_DEPARTMENT VALUES(1,1,10,'ACCOUNTING','NEW YORK',1);
                    INSERT INTO COMP_KEY_DEPARTMENT VALUES(2,2,20,'RESEARCH','DALLAS',1);
                    INSERT INTO COMP_KEY_DEPARTMENT VALUES(3,3,30,'SALES','CHICAGO',1);
                    INSERT INTO COMP_KEY_DEPARTMENT VALUES(4,4,40,'OPERATIONS','BOSTON',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(1,1,'STREET 1',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(2,2,'STREET 2',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(3,3,'STREET 3',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(4,4,'STREET 4',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(5,5,'STREET 5',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(6,6,'STREET 6',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(7,7,'STREET 7',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(8,8,'STREET 8',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(9,9,'STREET 9',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(10,10,'STREET 10',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(11,11,'STREET 11',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(12,12,'STREET 12',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(13,13,'STREET 13',1);
                    INSERT INTO COMP_KEY_ADDRESS VALUES(14,14,'STREET 14',1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(1,1,7369,'SMITH',13,13,'1980-12-17',800,2,2,1,1,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(2,2,7499,'ALLEN',6,6,'1981-02-20',1600,3,3,2,2,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(3,3,7521,'WARD',6,6,'1981-02-22',1250,3,3,3,3,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(4,4,7566,'JONES',9,9,'1981-04-02',2975,2,2,4,4,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(5,5,7654,'MARTIN',6,6,'1981-09-28',1250,3,3,5,5,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(6,6,7698,'BLAKE',9,9,'1981-05-01',2850,3,3,6,6,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(7,7,7782,'CLARK',9,9,'1981-06-09',2450,1,1,7,7,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(8,8,7788,'SCOTT',4,4,'1982-12-09',3000.0,2,2,8,8,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(9,9,7839,'KING',NULL,NULL,'1981-11-17',5000,1,1,9,9,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(10,10,7844,'TURNER',6,6,'1981-09-08',1500,3,3,10,10,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(11,11,7876,'ADAMS',8,8,'1983-01-12',1100,2,2,11,11,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(12,12,7900,'JAMES',6,6,'1981-12-03',950,3,3,12,12,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(13,13,7902,'FORD',4,4,'1981-12-03',3000,2,2,13,13,1);
                    INSERT INTO COMP_KEY_EMPLOYEE VALUES(14,14,7934,'MILLER',7,7,'1982-01-23',1300,1,1,14,14,1);

                    INSERT INTO TENSE VALUES (1, '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10');
                    INSERT INTO JOB VALUES (1, 'SALESMAN');
                    INSERT INTO JOB VALUES (2, 'MANAGER');
                    INSERT INTO JOB VALUES (3, 'PRESIDENT');
                    INSERT INTO AUTHORITY VALUES (1, 10);
                    INSERT INTO AUTHORITY VALUES (2, 20);
                    INSERT INTO AUTHORITY VALUES (3, 30);
                    INSERT INTO NO_ID VALUES (1, 1);
                    INSERT INTO NO_ID VALUES (1, 1);

                    INSERT INTO ID_GENERATOR VALUES('TABLE_STRATEGY_ID', 1);
                    """.trimIndent()
                )
            }
        }
    }

    @AfterEach
    fun after() {
        config.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute("DROP ALL OBJECTS")
            }
        }
    }

    @Nested
    inner class FindByIdTest {

        @Test
        fun test() {
            val db = Db(config)
            val address = db.findById<Address>(2)
            assertEquals(Address(2, "STREET 2", 1), address)
        }

        @Test
        fun idAndVersion() {
            val db = Db(config)
            val address = db.findById<Address>(2, 1)
            assertEquals(Address(2, "STREET 2", 1), address)
        }

        @Test
        fun idList() {
            val db = Db(config)
            val address = db.findById<CompositeKeyAddress>(listOf(2, 2))
            assertEquals(CompositeKeyAddress(2, 2, "STREET 2", 1), address)
        }

        @Test
        fun idListAndVersion() {
            val db = Db(config)
            val address = db.findById<CompositeKeyAddress>(listOf(2, 2), 1)
            assertEquals(CompositeKeyAddress(2, 2, "STREET 2", 1), address)
        }

        @Test
        fun embedded() {
            val db = Db(config)
            val employee = db.findById<Employee>(1)
            assertNotNull(employee)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val employee = db.findById<Worker>(1)
            assertNotNull(employee)
        }
    }

    @Nested
    inner class SelectTest {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId ge 1
                }
                orderBy {
                    Address::addressId.desc()
                }
                limit { 2 }
                offset { 5 }
            }
            assertEquals(
                listOf(
                    Address(10, "STREET 10", 1),
                    Address(9, "STREET 9", 1)
                ), list
            )
        }

        @Test
        fun criteriaScopeProperties() {
            val db = Db(config)
            val list = db.select<Address> {
                where = WhereScope().apply {
                    Address::addressId ge 1
                }
                orderBy = OrderByScope().apply {
                    Address::addressId.desc()
                }
                limit = 2
                offset = 5
            }
            assertEquals(
                listOf(
                    Address(10, "STREET 10", 1),
                    Address(9, "STREET 9", 1)
                ), list
            )
        }

        @Test
        fun like() {
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Address::street like "STREET 1_"
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((10..15).toList(), idList)
        }

        @Test
        fun notLike() {
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Address::street notLike "STREET 1_"
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((1..9).toList(), idList)
        }

        @Test
        fun noArg() {
            val db = Db(config)
            val list = db.select<Address>()
            assertEquals(15, list.size)
        }

        @Test
        fun not() {
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Address::addressId gt 5
                    not {
                        Address::addressId ge 10
                    }
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((6..9).toList(), idList)
        }

        @Test
        fun and() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId ge 1
                    and {
                        Address::addressId ge 1
                    }
                }
                orderBy {
                    Address::addressId.desc()
                }
                limit { 2 }
                offset { 5 }
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
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId ge 1
                    or {
                        Address::addressId ge 1
                        Address::addressId ge 1
                    }
                }
                orderBy {
                    Address::addressId.desc()
                }
                limit { 2 }
                offset { 5 }
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
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId `in` listOf(9, 10)
                }
                orderBy {
                    Address::addressId.desc()
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
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Address::addressId notIn (1..9).toList()
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((10..15).toList(), idList)
        }

        @Test
        fun in_empty() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId `in` emptyList()
                }
                orderBy {
                    Address::addressId.desc()
                }
            }
            assertTrue(list.isEmpty())
        }

        @Test
        fun in2() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId to Address::street `in` listOf(9 to "STREET 9", 10 to "STREET 10")
                }
                orderBy {
                    Address::addressId.desc()
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
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Address::addressId to Address::street notIn listOf(1 to "STREET 1", 2 to "STREET 2")
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((3..15).toList(), idList)
        }

        @Test
        fun in2_empty() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId to Address::street `in` emptyList()
                }
                orderBy {
                    Address::addressId.desc()
                }
            }
            assertTrue(list.isEmpty())
        }

        @Test
        fun in3() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Triple(Address::addressId, Address::street, Address::version) `in` listOf(
                        Triple(9, "STREET 9", 1),
                        Triple(10, "STREET 10", 1)
                    )
                }
                orderBy {
                    Address::addressId.desc()
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
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Triple(Address::addressId, Address::street, Address::version) notIn listOf(
                        Triple(1, "STREET 1", 1),
                        Triple(2, "STREET 2", 1)
                    )
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((3..15).toList(), idList)
        }

        @Test
        fun in3_empty() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Triple(Address::addressId, Address::street, Address::version) `in` emptyList()
                }
                orderBy {
                    Address::addressId.desc()
                }
            }
            assertTrue(list.isEmpty())
        }

        @Test
        fun between() {
            val db = Db(config)
            val idList = db.select<Address> {
                where {
                    Address::addressId between (5 to 10)
                }
                orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((5..10).toList(), idList)
        }

        @Test
        fun isNull() {
            val db = Db(config)
            val idList = db.select<Employee> {
                where {
                    Employee::managerId eq null
                }
            }.map { it.employeeId }
            assertEquals(listOf(9), idList)
        }

        @Test
        fun isNotNull() {
            val db = Db(config)
            val idList = db.select<Employee> {
                where {
                    Employee::managerId ne null
                }
            }.map { it.employeeId }
            assertTrue(9 !in idList)
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.select<Address, List<Address>>({
                where {
                    Address::addressId ge 1
                }
                orderBy {
                    Address::addressId.desc()
                }
                limit { 2 }
                offset { 5 }
            }) {
                it.toList()
            }
            assertEquals(
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
            val db = Db(config)
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

        @Test
        fun forUpdate() {
            val db = Db(config)
            val list = db.select<Address> {
                where {
                    Address::addressId ge 1
                }
                orderBy {
                    Address::addressId.desc()
                }
                limit { 2 }
                offset { 5 }
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
            val db = Db(config)
            val list = db.select<Employee> {
                where {
                    EmployeeDetail::salary ge BigDecimal("2000.00")
                }
            }
            assertEquals(6, list.size)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val list = db.select<Worker> {
                where {
                    WorkerSalary::salary ge BigDecimal("2000.00")
                }
            }
            assertEquals(6, list.size)
        }
    }

    @Nested
    inner class QueryTest {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.query<Address>("select * from address")
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun expand() {
            val db = Db(config)
            val list = db.query<Address>("select /*%expand*/* from address")
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.query<Address, List<Address>>("select * from address") {
                it.toList()
            }
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun sequence_expand() {
            val db = Db(config)
            val list = db.query<Address, List<Address>>("select /*%expand*/* from address") {
                it.toList()
            }
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun condition_objectExpression() {
            val db = Db(config)
            val list =
                db.query<Address>(
                    "select * from address where street = /*street*/'test'", object {
                        val street = "STREET 10"
                    }
                )
            assertEquals(1, list.size)
            assertEquals(Address(10, "STREET 10", 1), list[0])
        }

        @Test
        fun condition_dataClass() {
            data class Condition(val street: String)

            val db = Db(config)
            val list =
                db.query<Address>(
                    "select * from address where street = /*street*/'test'", Condition("STREET 10")
                )
            assertEquals(1, list.size)
            assertEquals(Address(10, "STREET 10", 1), list[0])
        }

        @Test
        fun embedded() {
            val db = Db(config)
            val list =
                db.query<Employee>(
                    """
                select employee_id, employee_no, employee_name, manager_id,
                hiredate, salary, department_id, address_id, version from employee
            """.trimIndent()
                )
            assertEquals(14, list.size)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val list =
                db.query<Worker>(
                    """
                select employee_id, employee_no, employee_name, manager_id,
                hiredate, salary, department_id, address_id, version from employee
                """.trimIndent()
                )
            assertEquals(14, list.size)
        }

        @Test
        fun `in`() {
            val db = Db(config)
            val list = db.query<Address>(
                "select * from address where address_id in /*list*/(0)",
                object {
                    val list = listOf(1, 2)
                }
            )
            assertEquals(2, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
            assertEquals(Address(2, "STREET 2", 1), list[1])
        }

        @Test
        fun in2() {
            val db = Db(config)
            val list = db.query<Address>(
                "select * from address where (address_id, street) in /*pairs*/(0, '')",
                object {
                    val pairs = listOf(1 to "STREET 1", 2 to "STREET 2")
                }
            )
            assertEquals(2, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
            assertEquals(Address(2, "STREET 2", 1), list[1])
        }

        @Test
        fun in3() {
            val db = Db(config)
            val list = db.query<Address>(
                "select * from address where (address_id, street, version) in /*triples*/(0, '', 0)",
                object {
                    val triples = listOf(Triple(1, "STREET 1", 1), Triple(2, "STREET 2", 1))
                }
            )
            assertEquals(2, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
            assertEquals(Address(2, "STREET 2", 1), list[1])
        }
    }

    @Nested
    inner class PaginateTest {

        @Test
        fun test() {
            val db = Db(config)
            val (list, count) = db.paginate<Address>("select * from address", limit = 3, offset = 5)
            assertEquals(3, list.size)
            assertEquals(Address(6, "STREET 6", 1), list[0])
            assertEquals(Address(7, "STREET 7", 1), list[1])
            assertEquals(Address(8, "STREET 8", 1), list[2])
            assertEquals(15, count)
        }
    }

    @Nested
    inner class SelectOneColumnTest {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.queryOneColumn<String>("select street from address")
            assertEquals(15, list.size)
            assertEquals("STREET 1", list[0])
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.queryOneColumn<String?, List<String?>>("select street from address") {
                it.toList()
            }
            assertEquals(15, list.size)
            assertEquals("STREET 1", list[0])
        }
    }

    @Nested
    inner class SelectTowColumnsTest {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.queryTwoColumns<Int, String>("select address_id, street from address")
            assertEquals(15, list.size)
            assertEquals(1, list[0].first)
            assertEquals("STREET 1", list[0].second)
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.queryTwoColumns<Int, String?, List<Pair<Int, String?>>>(
                "select address_id, street from address"
            ) { it.toList() }
            assertEquals(15, list.size)
            assertEquals(1, list[0].first)
            assertEquals("STREET 1", list[0].second)
        }
    }

    @Nested
    inner class SelectThreeColumnsTest {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.queryThreeColumns<Int, String, Int>("select address_id, street, version from address")
            assertEquals(15, list.size)
            assertEquals(15, list[14].first)
            assertEquals("STREET 15", list[14].second)
            assertEquals(1, list[0].third)
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.queryThreeColumns<Int, String?, Int, List<Triple<Int, String?, Int>>>(
                "select address_id, street, version from address"
            ) { it.toList() }
            assertEquals(15, list.size)
            assertEquals(15, list[14].first)
            assertEquals("STREET 15", list[14].second)
            assertEquals(1, list[14].third)
        }
    }

    @Nested
    inner class DeleteTest {

        @Test
        fun test() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.query<Address>(sql).first()
            db.delete(address)
            val address2 = db.query<Address>(sql).firstOrNull()
            assertNull(address2)
        }

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        @Test
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preDelete(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postDelete(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val sql = "select * from address where address_id = 15"
            val address = db.query<Address>(sql).first()
            val address2 = db.delete(address)
            assertEquals(Address(15, "*STREET 15*", 1), address2)
            val address3 = db.query<Address>(sql).firstOrNull()
            assertNull(address3)
        }

        @Test
        fun optimisticLockException() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.query<Address>(sql).first()
            db.delete(address)
            assertThrows<OptimisticLockException> { db.delete(address) }
        }

        @Test
        fun embedded() {
            val db = Db(config)
            val employee = Employee(
                employeeId = 100,
                employeeNo = 9999,
                employeeName = "aaa",
                managerId = null,
                detail = EmployeeDetail(LocalDate.of(2019, 6, 15), BigDecimal("2000.00")),
                departmentId = 1,
                addressId = 1,
                version = 1
            )
            db.insert(employee)
            val employee2 = db.findById<Employee>(100)
            assertEquals(employee, employee2)
            db.delete(employee)
            val employee3 = db.findById<Employee>(100)
            assertNull(employee3)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val salary = WorkerSalary(BigDecimal("2000.00"))
            val worker = Worker(
                employeeId = 100,
                employeeNo = 9999,
                employeeName = "aaa",
                managerId = null,
                detail = WorkerDetail(LocalDate.of(2019, 6, 15), salary),
                departmentId = 1,
                addressId = 1,
                version = 1
            )
            db.insert(worker)
            val worker2 = db.findById<Worker>(100)
            assertEquals(worker, worker2)
            db.delete(worker)
            val worker3 = db.findById<Worker>(100)
            assertNull(worker3)
        }

        @Test
        fun embedded_valueAssignment() {
            val db = Db(config)
            val human = Human(
                name = "aaa",
                common = Common()
            )
            val human2 = db.insert(human)
            db.delete(human2)
            val human3 = db.findById<Human>(1)
            assertNull(human3)
        }
    }

    @Nested
    inner class InsertTest {

        @Test
        fun test() {
            val db = Db(config)
            val address = Address(16, "STREET 16", 0)
            db.insert(address)
            val address2 = db.query<Address>("select * from address where address_id = 16").firstOrNull()
            assertEquals(address, address2)
        }

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        @Test
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preInsert(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postInsert(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val address = Address(16, "STREET 16", 0)
            val address2 = db.insert(address)
            assertEquals(Address(16, "*STREET 16*", 0), address2)
            val address3 = db.query<Address>("select * from address where address_id = 16").firstOrNull()
            assertEquals(Address(16, "*STREET 16", 0), address3)
        }

        @Test
        fun createdAt() {
            val db = Db(config)
            val person = Person(1, "ABC")
            val newPerson = db.insert(person)
            println(newPerson)
            assertTrue(newPerson.createdAt > person.createdAt)
        }

        @Test
        fun uniqueConstraintException() {
            val db = Db(config)
            val address = Address(1, "STREET 1", 0)
            assertThrows<UniqueConstraintException> { db.insert(address) }
        }

        @Test
        fun sequenceGenerator() {
            val db = Db(config)
            for (i in 1..201) {
                val strategy = SequenceStrategy(0, "test")
                val newStrategy = db.insert(strategy)
                assertEquals(i, newStrategy.id)
            }
        }

        @Test
        fun multiSequenceGenerator() {
            val db = Db(config)
            for (i in 1..201) {
                val strategy = MultiSequenceStrategy(0, 0L)
                val newStrategy = db.insert(strategy)
                assertEquals(MultiSequenceStrategy(i, i.toLong()), newStrategy)
            }
        }

        @Test
        fun embedded() {
            val db = Db(config)
            val employee = Employee(
                employeeId = 100,
                employeeNo = 9999,
                employeeName = "aaa",
                managerId = null,
                detail = EmployeeDetail(LocalDate.of(2019, 6, 15), BigDecimal("2000.00")),
                departmentId = 1,
                addressId = 1,
                version = 1
            )
            db.insert(employee)
            val employee2 = db.findById<Employee>(100)
            assertEquals(employee, employee2)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val salary = WorkerSalary(BigDecimal("2000.00"))
            val worker = Worker(
                employeeId = 100,
                employeeNo = 9999,
                employeeName = "aaa",
                managerId = null,
                detail = WorkerDetail(LocalDate.of(2019, 6, 15), salary),
                departmentId = 1,
                addressId = 1,
                version = 1
            )
            db.insert(worker)
            val worker2 = db.findById<Worker>(100)
            assertEquals(worker, worker2)
        }

        @Test
        fun embedded_valueAssignment() {
            val db = Db(config)
            val human = Human(
                name = "aaa",
                common = Common()
            )
            val human2 = db.insert(human)
            val human3 = db.findById<Human>(1, 0)
            assertEquals(human2, human3)
        }
    }

    @Nested
    inner class UpdateTest {

        @Test
        fun test() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.query<Address>(sql).first()
            val newAddress = address.copy(street = "NY street")
            db.update(newAddress)
            val address2 = db.query<Address>(sql).firstOrNull()
            assertEquals(Address(15, "NY street", 2), address2)
        }

        @Test
        fun updatedAt() {
            val db = Db(config)
            val person = Person(1, "ABC")
            val newPerson = db.insert(person).let {
                db.update(it)
            }
            assertTrue(newPerson.updatedAt > person.updatedAt)
        }

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preUpdate(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postUpdate(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val sql = "select * from address where address_id = 15"
            val address = db.query<Address>(sql).first()
            val newAddress = address.copy(street = "NY street")
            val address2 = db.update(newAddress)
            assertEquals(Address(15, "*NY street*", 2), address2)
            val address3 = db.query<Address>(sql).firstOrNull()
            assertEquals(Address(15, "*NY street", 2), address3)
        }

        @Test
        fun uniqueConstraintException() {
            val db = Db(config)
            val address = Address(1, "STREET 2", 1)
            assertThrows<UniqueConstraintException> { db.update(address) }
        }

        @Test
        fun optimisticLockException() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.query<Address>(sql).first()
            db.update(address)
            assertThrows<OptimisticLockException> { db.update(address) }
        }

        @Test
        fun embedded() {
            val db = Db(config)
            val employee = Employee(
                employeeId = 100,
                employeeNo = 9999,
                employeeName = "aaa",
                managerId = null,
                detail = EmployeeDetail(LocalDate.of(2019, 6, 15), BigDecimal("2000.00")),
                departmentId = 1,
                addressId = 1,
                version = 1
            )
            db.insert(employee)
            val employee2 = db.findById<Employee>(100)
            assertEquals(employee, employee2)

            val employee3 = employee.copy(detail = employee.detail.copy(salary = BigDecimal("5000.00")))
            val employee4 = db.update(employee3)
            assertEquals(BigDecimal("5000.00"), employee4.detail.salary)

            val employee5 = db.findById<Employee>(100)
            assertEquals(BigDecimal("5000.00"), employee5?.detail?.salary)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val salary = WorkerSalary(BigDecimal("2000.00"))
            val worker = Worker(
                employeeId = 100,
                employeeNo = 9999,
                employeeName = "aaa",
                managerId = null,
                detail = WorkerDetail(LocalDate.of(2019, 6, 15), salary),
                departmentId = 1,
                addressId = 1,
                version = 1
            )
            db.insert(worker)
            val worker2 = db.findById<Worker>(100)
            assertEquals(worker, worker2)

            val worker3 = worker.copy(
                detail = worker.detail.copy(
                    salary = WorkerSalary(
                        BigDecimal("5000.00")
                    )
                )
            )
            val worker4 = db.update(worker3)
            assertEquals(WorkerSalary(BigDecimal("5000.00")), worker4.detail.salary)

            val worker5 = db.findById<Worker>(100)
            assertEquals(WorkerSalary(BigDecimal("5000.00")), worker5?.detail?.salary)
        }

        @Test
        fun embedded_valueAssignment() {
            val db = Db(config)
            val human = Human(
                name = "aaa",
                common = Common()
            )
            val human2 = db.insert(human)
            val human3 = human2.copy(name = "bbb")
            val human4 = db.update(human3)
            val human5 = db.findById<Human>(1)
            assertEquals(human4, human5)
            println(human4)
        }
    }

    @Nested
    inner class MergeTest {

        @Test
        fun insert_keys() {
            val db = Db(config)
            val department = Department(5, 50, "PLANNING", "TOKYO", 0)
            db.merge(department, Department::departmentNo)
            val department2 = db.findById<Department>(5)
            assertEquals(department, department2)
        }

        @Test
        fun insert_noKeys() {
            val db = Db(config)
            val department = Department(5, 50, "PLANNING", "TOKYO", 0)
            db.merge(department)
            val department2 = db.findById<Department>(5)
            assertEquals(department, department2)
        }

        @Test
        fun update_keys() {
            val db = Db(config)
            val department = Department(5, 10, "PLANNING", "TOKYO", 0)
            db.merge(department, Department::departmentNo)
            assertNull(db.findById<Department>(5))
            assertEquals(department.copy(departmentId = 1), db.findById<Department>(1))
        }

        @Test
        fun update_noKeys() {
            val db = Db(config)
            val department = Department(1, 50, "PLANNING", "TOKYO", 0)
            db.merge(department)
            val department2 = db.findById<Department>(1)
            assertEquals(department, department2)
        }

        @Test
        fun uniqueConstraintException() {
            val db = Db(config)
            val department = db.findById<Department>(1)!!
            assertThrows<UniqueConstraintException> { db.merge(department.copy(departmentId = 2)) }
        }
    }

    @Nested
    inner class BatchDeleteTest {

        @Test
        fun test() {
            val db = Db(config)
            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            db.batchInsert(addressList)
            val sql = "select * from address where address_id in (16, 17, 18)"
            assertEquals(addressList, db.query<Address>(sql))
            db.batchDelete(addressList)
            assertTrue(db.query<Address>(sql).isEmpty())
        }

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preDelete(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postDelete(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            db.batchInsert(addressList)
            val sql = "select * from address where address_id in (16, 17, 18)"
            assertEquals(addressList, db.query<Address>(sql))
            val list = db.batchDelete(addressList)
            assertEquals(
                listOf(
                    Address(16, "*STREET 16*", 0),
                    Address(17, "*STREET 17*", 0),
                    Address(18, "*STREET 18*", 0)
                ), list
            )
            assertTrue(db.query<Address>(sql).isEmpty())
        }

        @Test
        fun optimisticLockException() {
            val db = Db(config)
            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            db.batchInsert(addressList)
            assertThrows<OptimisticLockException> {
                db.batchDelete(
                    listOf(
                        addressList[0],
                        addressList[1],
                        addressList[2].copy(version = +1)
                    )
                )
            }
        }
    }

    @Nested
    inner class BatchInsertTest {

        @Test
        fun test() {
            val db = Db(config)
            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            db.batchInsert(addressList)
            val list = db.query<Address>("select * from address where address_id in (16, 17, 18)")
            assertEquals(addressList, list)
        }

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preInsert(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postInsert(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            val list = db.batchInsert(addressList)
            assertEquals(
                listOf(
                    Address(16, "*STREET 16*", 0),
                    Address(17, "*STREET 17*", 0),
                    Address(18, "*STREET 18*", 0)
                ), list
            )
            val list2 = db.query<Address>("select * from address where address_id in (16, 17, 18)")
            assertEquals(
                listOf(
                    Address(16, "*STREET 16", 0),
                    Address(17, "*STREET 17", 0),
                    Address(18, "*STREET 18", 0)
                ), list2
            )
        }

        @Test
        fun createdAt() {
            val db = Db(config)
            val personList = listOf(
                Person(1, "A"),
                Person(2, "B"),
                Person(3, "C")
            )
            db.batchInsert(personList)
            val list = db.query<Person>("select /*%expand*/* from person")
            assertTrue(list.all { it.createdAt > LocalDateTime.MIN })
        }

        @Test
        fun uniqueConstraintException() {
            val db = Db(config)
            assertThrows<UniqueConstraintException> {
                db.batchInsert(
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0)
                    )
                )
            }
        }
    }

    @Nested
    inner class BatchUpdateTest {

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preUpdate(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postUpdate(entity: T, desc: EntityDesc<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val sql = "select * from address where address_id in (1,2,3)"
            val addressList = db.query<Address>(sql)
            val list = db.batchUpdate(addressList)
            assertEquals(
                listOf(
                    Address(1, "*STREET 1*", 2),
                    Address(2, "*STREET 2*", 2),
                    Address(3, "*STREET 3*", 2)
                ), list
            )
            val list2 = db.query<Address>(sql)
            assertEquals(
                listOf(
                    Address(1, "*STREET 1", 2),
                    Address(2, "*STREET 2", 2),
                    Address(3, "*STREET 3", 2)
                ), list2
            )
        }

        @Test
        fun updatedAt() {
            val db = Db(config)
            val personList = listOf(
                Person(1, "A"),
                Person(2, "B"),
                Person(3, "C")
            )
            db.batchInsert(personList)
            db.query<Person>("select /*%expand*/* from person").let {
                db.batchUpdate(it)
            }
            val list = db.query<Person>("select /*%expand*/* from person")
            assertTrue(list.all { it.updatedAt > LocalDateTime.MIN })
        }

        @Test
        fun uniqueConstraintException() {
            val db = Db(config)
            assertThrows<UniqueConstraintException> {
                db.batchUpdate(
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "B", 1)
                    )
                )
            }
        }

        @Test
        fun optimisticLockException() {
            val db = Db(config)
            assertThrows<OptimisticLockException> {
                db.batchUpdate(
                    listOf(
                        Address(1, "A", 1),
                        Address(2, "B", 1),
                        Address(3, "C", 2)
                    )
                )
            }
        }
    }

    @Nested
    inner class BatchMergeTest {

        @Test
        fun keys() {
            val db = Db(config)
            val departments = listOf(
                Department(5, 50, "PLANNING", "TOKYO", 0),
                Department(6, 10, "DEVELOPMENT", "KYOTO", 0)
            )
            db.batchMerge(departments, Department::departmentNo)
            assertEquals(departments[0], db.findById<Department>(5))
            assertNull(db.findById<Department>(6))
            assertEquals(departments[1].copy(departmentId = 1), db.findById<Department>(1))
        }

        @Test
        fun noKeys() {
            val db = Db(config)
            val departments = listOf(
                Department(5, 50, "PLANNING", "TOKYO", 0),
                Department(1, 60, "DEVELOPMENT", "KYOTO", 0)
            )
            db.batchMerge(departments)
            assertEquals(departments[0], db.findById<Department>(5))
            assertEquals(departments[1], db.findById<Department>(1))
        }

        @Test
        fun uniqueConstraintException() {
            val db = Db(config)
            val department = db.findById<Department>(1)!!
            assertThrows<UniqueConstraintException> { db.batchMerge(listOf(department.copy(departmentId = 2))) }
        }
    }

    @Nested
    inner class ExecuteUpdateTest {

        @Test
        fun test() {
            val db = Db(config)
            val count = db.executeUpdate("update address set street = /*street*/'' where address_id = /*id*/0", object {
                val id = 15
                val street = "NY street"
            })
            assertEquals(1, count)
            val address = db.query<Address>("select * from address where address_id = 15").firstOrNull()
            assertEquals(Address(15, "NY street", 1), address)
        }
    }

    @Nested
    inner class ExecuteTest {

        @Test
        fun test() {
            val db = Db(config)
            db.execute(
                """
                create table execute_table(value varchar(20));
                insert into execute_table(value) values('test');
                """.trimIndent()
            )
            val value = db.queryOneColumn<String>("select value from execute_table").firstOrNull()
            assertEquals("test", value)
        }
    }

    @Nested
    inner class DryRunTest {

        @Test
        fun findById() {
            val db = Db(config)
            val (sql) = db.dryRun.findById<Address>(2)
            println(sql)
        }

        @Test
        fun select() {
            val db = Db(config)
            val (sql) = db.dryRun.select<Address> {
                where {
                    Address::addressId ge 1
                }
                orderBy {
                    Address::addressId.desc()
                }
                limit { 2 }
                offset { 5 }
            }
            println(sql)
        }

        @Test
        fun query() {
            val db = Db(config)
            val (sql) = db.dryRun.query<Address>("select * from address")
            println(sql)
        }

        @Test
        fun paginate() {
            val db = Db(config)
            val (sql) = db.dryRun.paginate<Address>("select * from address", limit = 3, offset = 5)
            println(sql)
        }

        @Test
        fun queryOneColumn() {
            val db = Db(config)
            val sql = db.dryRun.queryOneColumn("select street from address")
            println(sql)
        }

        @Test
        fun queryTwoColumns() {
            val db = Db(config)
            val sql = db.dryRun.queryTwoColumns("select address_id, street from address")
            println(sql)
        }

        @Test
        fun queryThreeColumns() {
            val db = Db(config)
            val sql = db.dryRun.queryThreeColumns("select address_id, street, version from address")
            println(sql)
        }

        @Test
        fun delete() {
            val db = Db(config)
            val address = db.query<Address>("select * from address where address_id = 15").first()
            val (sql) = db.dryRun.delete(address)
            println(sql)
        }

        @Test
        fun insert() {
            val db = Db(config)
            val strategy = SequenceStrategy(-100, "a")
            val (sql) = db.dryRun.insert(strategy)
            assertEquals("insert into SEQUENCE_STRATEGY (id, value) values (0, 'a')", sql.log)
        }

        @Test
        fun update() {
            val db = Db(config)
            val address = db.query<Address>("select * from address where address_id = 15").first()
            val newAddress = address.copy(street = "NY street")
            val (sql) = db.dryRun.update(newAddress)
            println(sql)
        }

        @Test
        fun merge() {
            val db = Db(config)
            val department = Department(5, 50, "PLANNING", "TOKYO", 0)
            val (sql) = db.dryRun.merge(department, Department::departmentNo)
            println(sql)
        }

        @Test
        fun batchDelete() {
            val db = Db(config)
            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            val (sqls) = db.dryRun.batchDelete(addressList)
            assertEquals(3, sqls.size)
        }

        @Test
        fun batchInsert() {
            val db = Db(config)
            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            val (sqls) = db.dryRun.batchInsert(addressList)
            assertEquals(3, sqls.size)
        }

        @Test
        fun batchUpdate() {
            val db = Db(config)
            val personList = listOf(
                Person(1, "A"),
                Person(2, "B"),
                Person(3, "C")
            )
            val (sqls) = db.dryRun.batchUpdate(personList)
            assertEquals(3, sqls.size)
        }

        @Test
        fun batchMerge() {
            val db = Db(config)
            val departments = listOf(
                Department(5, 50, "PLANNING", "TOKYO", 0),
                Department(6, 10, "DEVELOPMENT", "KYOTO", 0)
            )
            val (sqls) = db.dryRun.batchMerge(departments, Department::departmentNo)
            assertEquals(2, sqls.size)
        }
    }

    data class AnyPerson(val name: String) : Serializable
    data class AnyTest(val id: Int, val value: Any)
    object AnyTestMetadata : EntityMetadata<AnyTest>({
        id(AnyTest::id)
    })

    @Suppress("ArrayInDataClass")
    data class ByteArrayTest(val id: Int, val value: ByteArray)
    object ByteArrayTestMetadata : EntityMetadata<ByteArrayTest>({
        id(ByteArrayTest::id)
    })

    data class BigDecimalTest(val id: Int, val value: BigDecimal)
    object BigDecimalTestMetadata : EntityMetadata<BigDecimalTest>({
        id(BigDecimalTest::id)
    })

    data class BigIntegerTest(val id: Int, val value: BigInteger)
    object BigIntegerTestMetadata : EntityMetadata<BigIntegerTest>({
        id(BigIntegerTest::id)
    })

    data class BooleanTest(val id: Int, val value: Boolean)
    object BooleanTestMetadata : EntityMetadata<BooleanTest>({
        id(BooleanTest::id)
    })

    data class ByteTest(val id: Int, val value: Byte)
    object ByteTestMetadata : EntityMetadata<ByteTest>({
        id(ByteTest::id)
    })

    data class DoubleTest(val id: Int, val value: Double)
    object DoubleTestMetadata : EntityMetadata<DoubleTest>({
        id(DoubleTest::id)
    })

    data class EnumTest(val id: Int, val value: Direction)
    object EnumTestMetadata : EntityMetadata<EnumTest>({
        id(EnumTest::id)
    })

    data class FloatTest(val id: Int, val value: Float)
    object FloatTestMetadata : EntityMetadata<FloatTest>({
        id(FloatTest::id)
    })

    data class IntTest(val id: Int, val value: Int)
    object IntTestMetadata : EntityMetadata<IntTest>({
        id(IntTest::id)
    })

    data class LocalDateTest(val id: Int, val value: LocalDate)
    object LocalDateTestMetadata : EntityMetadata<LocalDateTest>({
        id(LocalDateTest::id)
    })

    data class LocalDateTimeTest(val id: Int, val value: LocalDateTime)
    object LocalDateTimeTestMetadata : EntityMetadata<LocalDateTimeTest>({
        id(LocalDateTimeTest::id)
    })

    data class LocalTimeTest(val id: Int, val value: LocalTime)
    object LocalTimeTestMetadata : EntityMetadata<LocalTimeTest>({
        id(LocalTimeTest::id)
    })

    data class LongTest(val id: Int, val value: Long)
    object LongTestMetadata : EntityMetadata<LongTest>({
        id(LongTest::id)
    })

    data class OffsetDateTimeTest(val id: Int, val value: OffsetDateTime)
    object OffsetDateTimeTestMetadata : EntityMetadata<OffsetDateTimeTest>({
        id(OffsetDateTimeTest::id)
    })

    data class ShortTest(val id: Int, val value: Short)
    object ShortTestMetadata : EntityMetadata<ShortTest>({
        id(ShortTest::id)
    })

    data class StringTest(val id: Int, val value: String)
    object StringTestMetadata : EntityMetadata<StringTest>({
        id(StringTest::id)
    })

    @Nested
    inner class DataTypeTest {

        @Test
        fun any() {
            val db = Db(config)
            val data = AnyTest(1, AnyPerson("ABC"))
            db.insert(data)
            val data2 = db.findById<AnyTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun bigDecimal() {
            val db = Db(config)
            val data = BigDecimalTest(1, BigDecimal.TEN)
            db.insert(data)
            val data2 = db.findById<BigDecimalTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun bigInteger() {
            val db = Db(config)
            val data = BigIntegerTest(1, BigInteger.TEN)
            db.insert(data)
            val data2 = db.findById<BigIntegerTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun boolean() {
            val db = Db(config)
            val data = BooleanTest(1, true)
            db.insert(data)
            val data2 = db.findById<BooleanTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun byte() {
            val db = Db(config)
            val data = ByteTest(1, 10)
            db.insert(data)
            val data2 = db.findById<ByteTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun byteArray() {
            val db = Db(config)
            val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
            db.insert(data)
            val data2 = db.findById<ByteArrayTest>(1)
            assertEquals(data.id, data2!!.id)
            assertArrayEquals(data.value, data2.value)
        }

        @Test
        fun double() {
            val db = Db(config)
            val data = DoubleTest(1, 10.0)
            db.insert(data)
            val data2 = db.findById<DoubleTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun enum() {
            val db = Db(config)
            val data = EnumTest(1, Direction.EAST)
            db.insert(data)
            val data2 = db.findById<EnumTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun float() {
            val db = Db(config)
            val data = FloatTest(1, 10.0f)
            db.insert(data)
            val data2 = db.findById<FloatTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun int() {
            val db = Db(config)
            val data = IntTest(1, 10)
            db.insert(data)
            val data2 = db.findById<IntTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun localDateTime() {
            val db = Db(config)
            val data = LocalDateTimeTest(1, LocalDateTime.of(2019, 6, 1, 12, 11, 10))
            db.insert(data)
            val data2 = db.findById<LocalDateTimeTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun localDate() {
            val db = Db(config)
            val data = LocalDateTest(1, LocalDate.of(2019, 6, 1))
            db.insert(data)
            val data2 = db.findById<LocalDateTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun localTime() {
            val db = Db(config)
            val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
            db.insert(data)
            val data2 = db.findById<LocalTimeTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun long() {
            val db = Db(config)
            val data = LongTest(1, 10L)
            db.insert(data)
            val data2 = db.findById<LongTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun offsetDateTime() {
            val db = Db(config)
            val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
            val offset = ZoneOffset.ofHours(9)
            val value = OffsetDateTime.of(dateTime, offset)
            val data = OffsetDateTimeTest(1, value)
            db.insert(data)
            val data2 = db.findById<OffsetDateTimeTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun short() {
            val db = Db(config)
            val data = ShortTest(1, 10)
            db.insert(data)
            val data2 = db.findById<ShortTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun string() {
            val db = Db(config)
            val data = StringTest(1, "ABC")
            db.insert(data)
            val data2 = db.findById<StringTest>(1)
            assertEquals(data, data2)
        }
    }

    @Nested
    inner class QuotesTest {

        @Test
        fun test() {
            val messages = mutableListOf<String>()
            val logger = object : StdoutLogger() {
                override fun logSql(sql: Sql) {
                    val message = sql.log!!
                    println(message)
                    messages.add(message)
                }
            }
            val myConfig = config.copy(logger = logger)
            val db = Db(myConfig)
            db.insert(Quotes(id = 0, value = "aaa"))
            assertEquals(
                listOf(
                    "call next value for \"SEQUENCE_STRATEGY_ID\"",
                    "insert into \"SEQUENCE_STRATEGY\" (\"ID\", \"VALUE\") values (1, 'aaa')"
                ), messages
            )

            messages.clear()
            db.select<Quotes>().first()
            assertEquals(
                listOf(
                    "select t0_.\"ID\", t0_.\"VALUE\" from \"SEQUENCE_STRATEGY\" t0_"
                ), messages
            )
        }
    }
}
