package org.komapper

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.komapper.jdbc.H2Dialect
import org.komapper.jdbc.SimpleDataSource
import org.komapper.tx.TransactionIsolationLevel
import java.lang.IllegalStateException
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML

@Suppress("UNUSED")
internal class DbTxTest {

    data class Address(
        @Id
        val addressId: Int,
        val street: String,
        @Version
        val version: Int
    )

    val simpleDataSource = SimpleDataSource("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")

    val config = DbConfig(
        dataSource = simpleDataSource,
        dialect = H2Dialect(),
        useTransaction = true
    )

    @BeforeEach
    fun before() {
        simpleDataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE SEQUENCE SEQUENCE_STRATEGY_ID START WITH 1 INCREMENT BY 100;
                    CREATE SEQUENCE MY_SEQUENCE_STRATEGY_ID START WITH 1 INCREMENT BY 100;

                    CREATE TABLE DEPARTMENT(DEPARTMENT_ID INTEGER NOT NULL PRIMARY KEY, DEPARTMENT_NO INTEGER NOT NULL UNIQUE,DEPARTMENT_NAME VARCHAR(20),LOCATION VARCHAR(20) DEFAULT 'TOKYO', VERSION INTEGER);
                    CREATE TABLE ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
                    CREATE TABLE EMPLOYEE(EMPLOYEE_ID INTEGER NOT NULL PRIMARY KEY, EMPLOYEE_NO INTEGER NOT NULL ,EMPLOYEE_NAME VARCHAR(20),MANAGER_ID INTEGER,HIREDATE DATE,SALARY NUMERIC(7,2),DEPARTMENT_ID INTEGER,ADDRESS_ID INTEGER,VERSION INTEGER, CONSTRAINT FK_DEPARTMENT_ID FOREIGN KEY(DEPARTMENT_ID) REFERENCES DEPARTMENT(DEPARTMENT_ID), CONSTRAINT FK_ADDRESS_ID FOREIGN KEY(ADDRESS_ID) REFERENCES ADDRESS(ADDRESS_ID));

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

                    CREATE TABLE ARRAY_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE ARRAY);
                    CREATE TABLE BLOB_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BLOB);
                    CREATE TABLE CLOB_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE CLOB);
                    CREATE TABLE NCLOB_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE NCLOB);
                    CREATE TABLE SQL_XML_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE CLOB);

                    CREATE TABLE ID_GENERATOR(PK VARCHAR(20) NOT NULL PRIMARY KEY, VALUE INTEGER NOT NULL);
                    CREATE TABLE MY_ID_GENERATOR(MY_PK VARCHAR(20) NOT NULL PRIMARY KEY, MY_VALUE INTEGER NOT NULL);
                    CREATE TABLE AUTO_STRATEGY(ID INTEGER NOT NULL IDENTITY PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE IDENTITY_STRATEGY(ID INTEGER NOT NULL IDENTITY PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE SEQUENCE_STRATEGY(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE SEQUENCE_STRATEGY2(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE TABLE_STRATEGY(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));
                    CREATE TABLE TABLE_STRATEGY2(ID INTEGER NOT NULL PRIMARY KEY, VALUE VARCHAR(10));

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
        simpleDataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute("DROP ALL OBJECTS")
            }
        }
    }

    @Test
    fun select() {
        val db = Db(config)
        val list = db.transaction.required {
            db.select<DbTest.Address>("select * from address")
        }
        assertEquals(15, list.size)
        assertEquals(DbTest.Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun commit() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction.required {
            val address = db.select<Address>(sql).first()
            db.delete(address)
        }
        db.transaction.required {
            val address = db.select<Address>(sql).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun rollback() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        try {
            db.transaction.required {
                val address = db.select<Address>(sql).first()
                db.delete(address)
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        db.transaction.required {
            val address = db.select<Address>(sql).first()
            assertNotNull(address)
        }
    }

    @Test
    fun setRollbackOnly() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction.required {
            val address = db.select<Address>(sql).first()
            db.delete(address)
            assertFalse(isRollbackOnly())
            setRollbackOnly()
            assertTrue(isRollbackOnly())
        }
        db.transaction.required {
            val address = db.select<Address>(sql).first()
            assertNotNull(address)
        }
    }

    @Test
    fun isolationLevel() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction.required(TransactionIsolationLevel.SERIALIZABLE) {
            val address = db.select<Address>(sql).first()
            db.delete(address)
        }
        db.transaction.required {
            val address = db.select<Address>(sql).firstOrNull()
            assertNull(address)
        }
    }


    @Test
    fun required_required() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction.required {
            val address = db.select<Address>(sql).first()
            db.delete(address)
            required {
                val address2 = db.select<Address>(sql).firstOrNull()
                assertNull(address2)
            }
        }
        db.transaction.required {
            val address = db.select<Address>(sql).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun requiresNew() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction.requiresNew {
            val address = db.select<Address>(sql).first()
            db.delete(address)
            val address2 = db.select<Address>(sql).firstOrNull()
            assertNull(address2)
        }
        db.transaction.required {
            val address = db.select<Address>(sql).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun required_requiresNew() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction.required {
            val address = db.select<Address>(sql).first()
            db.delete(address)
            requiresNew {
                val address2 = db.select<Address>(sql).firstOrNull()
                assertNotNull(address2)
            }
        }
        db.transaction.required {
            val address = db.select<Address>(sql).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun invoke() {
        val sql = "select * from address where address_id = 15"
        val db = Db(config)
        db.transaction {
            val address = db.select<Address>(sql).first()
            db.delete(address)
        }
        db.transaction {
            val address = db.select<Address>(sql).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun `specify useTransaction`() {
        val config = DbConfig(
            dataSource = simpleDataSource,
            dialect = H2Dialect()
        )
        val db = Db(config)
        val exception =
            org.junit.jupiter.api.assertThrows<IllegalStateException> { db.transaction }
        println(exception)
    }

    @Nested
    inner class JdbcType {

        @Test
        fun array() {
            data class ArrayTest(@Id val id: Int, val value: java.sql.Array)

            val db = Db(config)
            db.transaction {
                val array = db.createArrayOf("INTEGER", listOf(10, 20, 30))
                val data = ArrayTest(1, array)
                db.insert(data)
                val data2 = db.findById<ArrayTest>(1)
                assertEquals(data.id, data2!!.id)
                assertArrayEquals(data.value.array as Array<*>, data2.value.array as Array<*>)
            }
        }

        @Test
        fun blob() {
            data class BlobTest(@Id val id: Int, val value: Blob)

            val db = Db(config)
            db.transaction {
                val blob = db.createBlob()
                val bytes = byteArrayOf(10, 20, 30)
                blob.setBytes(1, bytes)
                val data = BlobTest(1, blob)
                db.insert(data)
                val data2 = db.findById<BlobTest>(1)
                assertEquals(data.id, data2!!.id)
                assertArrayEquals(data.value.getBytes(1, 3), data2.value.getBytes(1, 3))
            }
        }

        @Test
        fun clob() {
            data class ClobTest(@Id val id: Int, val value: Clob)

            val db = Db(config)
            db.transaction {
                val clob = db.createClob()
                clob.setString(1, "ABC")
                val data = ClobTest(1, clob)
                db.insert(data)
                val data2 = db.findById<ClobTest>(1)
                assertEquals(data.id, data2!!.id)
                assertEquals(data.value.getSubString(1, 3), data2.value.getSubString(1, 3))
            }
        }

        @Test
        fun nclob() {
            data class NClobTest(@Id val id: Int, val value: NClob)

            val db = Db(config)
            db.transaction {
                val nclob = db.createNClob()
                nclob.setString(1, "ABC")
                val data = NClobTest(1, nclob)
                db.insert(data)
                val data2 = db.findById<NClobTest>(1)
                assertEquals(data.id, data2!!.id)
                assertEquals(data.value.getSubString(1, 3), data2.value.getSubString(1, 3))
            }
        }

        @Test
        fun sqlXml() {
            data class SqlXmlTest(@Id val id: Int, val value: SQLXML)

            val db = Db(config)
            db.transaction {
                val sqlXml = db.createSQLXML()
                sqlXml.string = """<xml a="v">Text</xml>"""
                val data = SqlXmlTest(1, sqlXml)
                db.insert(data)
                val data2 = db.findById<SqlXmlTest>(1)
                assertEquals(data.id, data2!!.id)
                assertEquals(data.value.string, data2.value.string)
            }
        }

    }
}
