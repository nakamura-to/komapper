package org.komapper

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.komapper.jdbc.H2Dialect
import org.komapper.jdbc.SimpleDataSource
import org.komapper.meta.EntityListener
import org.komapper.meta.EntityMeta
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Suppress("UNUSED")
internal class DbTest {

    data class Address(
        @Id
        val addressId: Int,
        val street: String,
        @Version
        val version: Int
    )

    @Table(name = "COMP_KEY_ADDRESS")
    data class CompositeKeyAddress(
        @Id
        val addressId1: Int,
        @Id
        val addressId2: Int,
        val street: String,
        @Version
        val version: Int
    )

    @Table(name = "SEQUENCE_STRATEGY")
    data class SequenceStrategy(
        @Id
        @SequenceGenerator(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100)
        val id: Int,
        val value: String
    )

    @Table(name = "SEQUENCE_STRATEGY")
    data class MultiSequenceStrategy(
        @Id
        @SequenceGenerator(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100)
        val id: Int,
        @Id
        @SequenceGenerator(name = "MY_SEQUENCE_STRATEGY_ID", incrementBy = 100)
        val value: Long
    )

    data class Person(
        @Id
        val personId: Int,
        val name: String,
        @CreatedAt
        val createdAt: LocalDateTime = LocalDateTime.MIN,
        @UpdatedAt
        val updatedAt: LocalDateTime = LocalDateTime.MIN
    )

    private enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    data class EmployeeDetail(
        val hiredate: LocalDate,
        val salary: BigDecimal
    )

    data class Employee(
        @Id
        val employeeId: Int,
        val employeeNo: Int,
        val employeeName: String,
        val managerId: Int?,
        @Embedded
        val detail: EmployeeDetail,
        val departmentId: Int,
        val addressId: Int,
        @Version
        val version: Int
    )

    data class WorkerSalary(val salary: BigDecimal)

    data class WorkerDetail(
        val hiredate: LocalDate,
        @Embedded
        val salary: WorkerSalary
    )

    @Table(name = "employee")
    data class Worker(
        @Id
        val employeeId: Int,
        val employeeNo: Int,
        val employeeName: String,
        val managerId: Int?,
        @Embedded
        val detail: WorkerDetail,
        val departmentId: Int,
        val addressId: Int,
        @Version
        val version: Int
    )

    data class Common(
        @Id
        @SequenceGenerator(name = "PERSON_ID_SEQUENCE", incrementBy = 100)
        val personId: Int = 0,
        @CreatedAt
        val createdAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0),
        @UpdatedAt
        val updatedAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0),
        @Version
        val version: Int = 0
    )

    @Table(name = "person")
    data class Human(
        val name: String,
        @Embedded
        val common: Common
    )

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
    inner class FindById {

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
    inner class Query {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.query<Address> {
                where {
                    Address::addressId ge 1
                }.orderBy {
                    Address::addressId.desc()
                }.limit(2).offset(5)
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
            val idList = db.query<Address> {
                where {
                    Address::street like "STREET 1_"
                }.orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((10..15).toList(), idList)
        }

        @Test
        fun notLike() {
            val db = Db(config)
            val idList = db.query<Address> {
                where {
                    Address::street notLike "STREET 1_"
                }.orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((1..9).toList(), idList)
        }

        @Test
        fun noArg() {
            val db = Db(config)
            val list = db.query<Address>()
            assertEquals(15, list.size)
        }

        @Test
        fun not() {
            val db = Db(config)
            val idList = db.query<Address> {
                where {
                    Address::addressId gt 5
                    not {
                        Address::addressId ge 10
                    }
                }.orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((6..9).toList(), idList)
        }

        @Test
        fun and() {
            val db = Db(config)
            val list = db.query<Address> {
                where {
                    Address::addressId ge 1
                    and {
                        Address::addressId ge 1
                    }
                }.orderBy {
                    Address::addressId.desc()
                }.limit(2).offset(5)
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
            val list = db.query<Address> {
                where {
                    Address::addressId ge 1
                    or {
                        Address::addressId ge 1
                        Address::addressId ge 1
                    }
                }.orderBy {
                    Address::addressId.desc()
                }.limit(2).offset(5)
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
            val list = db.query<Address> {
                where {
                    Address::addressId `in` listOf(9, 10)
                }.orderBy {
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
            val idList = db.query<Address> {
                where {
                    Address::addressId notIn (1..9).toList()
                }.orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((10..15).toList(), idList)
        }

        @Test
        fun in_empty() {
            val db = Db(config)
            val list = db.query<Address> {
                where {
                    Address::addressId `in` emptyList()
                }.orderBy {
                    Address::addressId.desc()
                }
            }
            assertTrue(list.isEmpty())
        }

        @Test
        fun between() {
            val db = Db(config)
            val idList = db.query<Address> {
                where {
                    Address::addressId between (5 to 10)
                }.orderBy {
                    Address::addressId.asc()
                }
            }.map { it.addressId }
            assertEquals((5..10).toList(), idList)
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.query<Address, List<Address>>({
                where {
                    Address::addressId ge 1
                }.orderBy {
                    Address::addressId.desc()
                }.limit(2).offset(5)
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
        fun embedded() {
            val db = Db(config)
            val list = db.query<Employee> {
                where {
                    EmployeeDetail::salary ge BigDecimal("2000.00")
                }
            }
            assertEquals(6, list.size)
        }

        @Test
        fun nestedEmbedded() {
            val db = Db(config)
            val list = db.query<Worker> {
                where {
                    WorkerSalary::salary ge BigDecimal("2000.00")
                }
            }
            assertEquals(6, list.size)
        }

    }

    @Nested
    inner class Select {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.select<Address>("select * from address")
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun expand() {
            val db = Db(config)
            val list = db.select<Address>("select /*%expand*/* from address")
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.select<Address, List<Address>>("select * from address") {
                it.toList()
            }
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun sequence_expand() {
            val db = Db(config)
            val list = db.select<Address, List<Address>>("select /*%expand*/* from address") {
                it.toList()
            }
            assertEquals(15, list.size)
            assertEquals(Address(1, "STREET 1", 1), list[0])
        }

        @Test
        fun condition_objectExpression() {
            val db = Db(config)
            val list =
                db.select<Address>(
                    "select * from address where street = /*street*/'test'"
                    , object {
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
                db.select<Address>(
                    "select * from address where street = /*street*/'test'"
                    , Condition("STREET 10")
                )
            assertEquals(1, list.size)
            assertEquals(Address(10, "STREET 10", 1), list[0])
        }

        @Test
        fun embedded() {
            val db = Db(config)
            val list =
                db.select<Employee>(
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
                db.select<Worker>(
                    """
                select employee_id, employee_no, employee_name, manager_id,
                hiredate, salary, department_id, address_id, version from employee
                """.trimIndent()
                )
            assertEquals(14, list.size)
        }
    }

    @Nested
    inner class SelectOneColumn {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.selectOneColumn<String>("select street from address")
            assertEquals(15, list.size)
            assertEquals("STREET 1", list[0])
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.selectOneColumn<String?, List<String?>>("select street from address") {
                it.toList()
            }
            assertEquals(15, list.size)
            assertEquals("STREET 1", list[0])
        }
    }

    @Nested
    inner class SelectTowColumns {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.selectTwoColumns<Int, String>("select address_id, street from address")
            assertEquals(15, list.size)
            assertEquals(1, list[0].first)
            assertEquals("STREET 1", list[0].second)
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.selectTwoColumns<Int, String?, List<Pair<Int, String?>>>(
                "select address_id, street from address"
            ) { it.toList() }
            assertEquals(15, list.size)
            assertEquals(1, list[0].first)
            assertEquals("STREET 1", list[0].second)
        }
    }

    @Nested
    inner class SelectThreeColumns {

        @Test
        fun test() {
            val db = Db(config)
            val list = db.selectThreeColumns<Int, String, Int>("select address_id, street, version from address")
            assertEquals(15, list.size)
            assertEquals(15, list[14].first)
            assertEquals("STREET 15", list[14].second)
            assertEquals(1, list[0].third)
        }

        @Test
        fun sequence() {
            val db = Db(config)
            val list = db.selectThreeColumns<Int, String?, Int, List<Triple<Int, String?, Int>>>(
                "select address_id, street, version from address"
            ) { it.toList() }
            assertEquals(15, list.size)
            assertEquals(15, list[14].first)
            assertEquals("STREET 15", list[14].second)
            assertEquals(1, list[14].third)
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun test() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.select<Address>(sql).first()
            db.delete(address)
            val address2 = db.select<Address>(sql).firstOrNull()
            assertNull(address2)
        }

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        @Test
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preDelete(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postDelete(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val sql = "select * from address where address_id = 15"
            val address = db.select<Address>(sql).first()
            val address2 = db.delete(address)
            assertEquals(Address(15, "*STREET 15*", 1), address2)
            val address3 = db.select<Address>(sql).firstOrNull()
            assertNull(address3)
        }

        @Test
        fun optimisticLockException() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.select<Address>(sql).first()
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
    inner class Insert {

        @Test
        fun test() {
            val db = Db(config)
            val address = Address(16, "STREET 16", 0)
            db.insert(address)
            val address2 = db.select<Address>("select * from address where address_id = 16").firstOrNull()
            assertEquals(address, address2)
        }

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        @Test
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preInsert(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postInsert(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val address = Address(16, "STREET 16", 0)
            val address2 = db.insert(address)
            assertEquals(Address(16, "*STREET 16*", 0), address2)
            val address3 = db.select<Address>("select * from address where address_id = 16").firstOrNull()
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
    inner class Update {

        @Test
        fun test() {
            val sql = "select * from address where address_id = 15"
            val db = Db(config)
            val address = db.select<Address>(sql).first()
            val newAddress = address.copy(street = "NY street")
            db.update(newAddress)
            val address2 = db.select<Address>(sql).firstOrNull()
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
                override fun <T> preUpdate(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postUpdate(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val sql = "select * from address where address_id = 15"
            val address = db.select<Address>(sql).first()
            val newAddress = address.copy(street = "NY street")
            val address2 = db.update(newAddress)
            assertEquals(Address(15, "*NY street*", 2), address2)
            val address3 = db.select<Address>(sql).firstOrNull()
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
            val address = db.select<Address>(sql).first()
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

            val worker3 = worker.copy(detail = worker.detail.copy(salary = WorkerSalary(BigDecimal("5000.00"))))
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
    inner class BatchDelete {

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
            assertEquals(addressList, db.select<Address>(sql))
            db.batchDelete(addressList)
            assertTrue(db.select<Address>(sql).isEmpty())
        }

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preDelete(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postDelete(entity: T, meta: EntityMeta<T>): T {
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
            assertEquals(addressList, db.select<Address>(sql))
            val list = db.batchDelete(addressList)
            assertEquals(
                listOf(
                    Address(16, "*STREET 16*", 0),
                    Address(17, "*STREET 17*", 0),
                    Address(18, "*STREET 18*", 0)
                ), list
            )
            assertTrue(db.select<Address>(sql).isEmpty())
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
    inner class BatchInsert {

        @Test
        fun test() {
            val db = Db(config)
            val addressList = listOf(
                Address(16, "STREET 16", 0),
                Address(17, "STREET 17", 0),
                Address(18, "STREET 18", 0)
            )
            db.batchInsert(addressList)
            val list = db.select<Address>("select * from address where address_id in (16, 17, 18)")
            assertEquals(addressList, list)
        }

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preInsert(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postInsert(entity: T, meta: EntityMeta<T>): T {
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
            val list2 = db.select<Address>("select * from address where address_id in (16, 17, 18)")
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
            val list = db.select<Person>("select * from person")
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
    inner class BatchUpdate {

        @Test
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        fun listener() {
            val db = Db(config.copy(listener = object : EntityListener {
                override fun <T> preUpdate(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T> postUpdate(entity: T, meta: EntityMeta<T>): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }))

            val sql = "select * from address where address_id in (1,2,3)"
            val addressList = db.select<Address>(sql)
            val list = db.batchUpdate(addressList)
            assertEquals(
                listOf(
                    Address(1, "*STREET 1*", 2),
                    Address(2, "*STREET 2*", 2),
                    Address(3, "*STREET 3*", 2)
                ), list
            )
            val list2 = db.select<Address>(sql)
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
            db.select<Person>("select * from person").let {
                db.batchUpdate(it)
            }
            val list = db.select<Person>("select * from person")
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
    inner class ExecuteUpdate {

        @Test
        fun test() {
            val db = Db(config)
            val count = db.executeUpdate("update address set street = /*street*/'' where address_id = /*id*/0", object {
                val id = 15
                val street = "NY street"
            })
            assertEquals(1, count)
            val address = db.select<Address>("select * from address where address_id = 15").firstOrNull()
            assertEquals(Address(15, "NY street", 1), address)
        }
    }

    @Nested
    inner class Execute {

        @Test
        fun test() {
            val db = Db(config)
            db.execute(
                """
                create table execute_table(value varchar(20));
                insert into execute_table(value) values('test');
                """.trimIndent()
            )
            val value = db.selectOneColumn<String>("select value from execute_table").firstOrNull()
            assertEquals("test", value)
        }
    }

    @Nested
    inner class JdbcType {

        @Test
        fun any() {
            data class Person(val name: String) : Serializable
            data class AnyTest(@Id val id: Int, val value: Any)

            val db = Db(config)
            val data = AnyTest(1, Person("ABC"))
            db.insert(data)
            val data2 = db.findById<AnyTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun bigDecimal() {
            data class BigDecimalTest(@Id val id: Int, val value: BigDecimal)

            val db = Db(config)
            val data = BigDecimalTest(1, BigDecimal.TEN)
            db.insert(data)
            val data2 = db.findById<BigDecimalTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun bigInteger() {
            data class BigIntegerTest(@Id val id: Int, val value: BigInteger)

            val db = Db(config)
            val data = BigIntegerTest(1, BigInteger.TEN)
            db.insert(data)
            val data2 = db.findById<BigIntegerTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun boolean() {
            data class BooleanTest(@Id val id: Int, val value: Boolean)

            val db = Db(config)
            val data = BooleanTest(1, true)
            db.insert(data)
            val data2 = db.findById<BooleanTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun byte() {
            data class ByteTest(@Id val id: Int, val value: Byte)

            val db = Db(config)
            val data = ByteTest(1, 10)
            db.insert(data)
            val data2 = db.findById<ByteTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun byteArray() {
            @Suppress("ArrayInDataClass")
            data class ByteArrayTest(@Id val id: Int, val value: ByteArray)

            val db = Db(config)
            val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
            db.insert(data)
            val data2 = db.findById<ByteArrayTest>(1)
            assertEquals(data.id, data2!!.id)
            assertArrayEquals(data.value, data2.value)
        }

        @Test
        fun double() {
            data class DoubleTest(@Id val id: Int, val value: Double)

            val db = Db(config)
            val data = DoubleTest(1, 10.0)
            db.insert(data)
            val data2 = db.findById<DoubleTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun enum() {
            data class EnumTest(@Id val id: Int, val value: Direction)

            val db = Db(config)
            val data = EnumTest(1, Direction.EAST)
            db.insert(data)
            val data2 = db.findById<EnumTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun float() {
            data class FloatTest(@Id val id: Int, val value: Float)

            val db = Db(config)
            val data = FloatTest(1, 10.0f)
            db.insert(data)
            val data2 = db.findById<FloatTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun int() {
            data class IntTest(@Id val id: Int, val value: Int)

            val db = Db(config)
            val data = IntTest(1, 10)
            db.insert(data)
            val data2 = db.findById<IntTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun localDateTime() {
            data class LocalDateTimeTest(@Id val id: Int, val value: LocalDateTime)

            val db = Db(config)
            val data = LocalDateTimeTest(1, LocalDateTime.of(2019, 6, 1, 12, 11, 10))
            db.insert(data)
            val data2 = db.findById<LocalDateTimeTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun localDate() {
            data class LocalDateTest(@Id val id: Int, val value: LocalDate)

            val db = Db(config)
            val data = LocalDateTest(1, LocalDate.of(2019, 6, 1))
            db.insert(data)
            val data2 = db.findById<LocalDateTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun localTime() {
            data class LocalTimeTest(@Id val id: Int, val value: LocalTime)

            val db = Db(config)
            val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
            db.insert(data)
            val data2 = db.findById<LocalTimeTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun long() {
            data class LongTest(@Id val id: Int, val value: Long)

            val db = Db(config)
            val data = LongTest(1, 10L)
            db.insert(data)
            val data2 = db.findById<LongTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun short() {
            data class ShortTest(@Id val id: Int, val value: Short)

            val db = Db(config)
            val data = ShortTest(1, 10)
            db.insert(data)
            val data2 = db.findById<ShortTest>(1)
            assertEquals(data, data2)
        }

        @Test
        fun string() {
            data class StringTest(@Id val id: Int, val value: String)

            val db = Db(config)
            val data = StringTest(1, "ABC")
            db.insert(data)
            val data2 = db.findById<StringTest>(1)
            assertEquals(data, data2)
        }

    }

}
