package org.komapper.jdbc.h2

import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.entity.DefaultEntityMetaResolver
import org.komapper.core.entity.EntityMeta
import org.komapper.core.entity.EntityMetaResolver
import org.komapper.core.entity.IdMeta
import org.komapper.core.entity.entities
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.sql.template
import org.komapper.core.tx.TransactionIsolationLevel

internal class TransactionTest {

    data class Address(
        val addressId: Int,
        val street: String,
        val version: Int
    )

    private val config = object : DbConfig() {
        override val dataSource = SimpleDataSource("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        override val dialect = H2Dialect()
        override val entityMetaResolver = DefaultEntityMetaResolver(
            entities {
                entity(Address::class) {
                    id(Address::addressId)
                    version(Address::version)
                }
            }
        )
    }

    private val db = Db(config)

    @BeforeEach
    fun before() {
        config.dataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE TABLE ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
                    CREATE TABLE ARRAY_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE ARRAY);
                    CREATE TABLE BLOB_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE BLOB);
                    CREATE TABLE CLOB_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE CLOB);
                    CREATE TABLE NCLOB_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE NCLOB);
                    CREATE TABLE SQL_XML_TEST(ID INTEGER NOT NULL PRIMARY KEY, VALUE CLOB);

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
                    """.trimIndent()
                )
            }
        }
    }

    @AfterEach
    fun after() {
        config.dataSource.connection.use { con ->
            con.createStatement().use { stmt ->
                stmt.execute("DROP ALL OBJECTS")
            }
        }
    }

    @Test
    fun select() {
        val list = db.transaction.required {
            db.select<Address>(template("select * from address"))
        }
        assertEquals(15, list.size)
        assertEquals(Address(1, "STREET 1", 1), list[0])
    }

    @Test
    fun commit() {
        val sql = template<Address>("select * from address where address_id = 15")
        db.transaction.required {
            val address = db.select(sql).first()
            db.delete(address)
        }
        db.transaction.required {
            val address = db.select(sql).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun rollback() {
        val sql = template<Address>("select * from address where address_id = 15")
        try {
            db.transaction.required {
                val address = db.select(sql).first()
                db.delete(address)
                throw Exception()
            }
        } catch (ignored: Exception) {
        }
        db.transaction.required {
            val address = db.select(sql).first()
            assertNotNull(address)
        }
    }

    @Test
    fun setRollbackOnly() {
        val sql = template<Address>("select * from address where address_id = 15")
        db.transaction.required {
            val address = db.select(sql).first()
            db.delete(address)
            assertFalse(isRollbackOnly())
            setRollbackOnly()
            assertTrue(isRollbackOnly())
        }
        db.transaction.required {
            val address = db.select(sql).first()
            assertNotNull(address)
        }
    }

    @Test
    fun isolationLevel() {
        val t = template<Address>("select * from address where address_id = 15")
        db.transaction.required(TransactionIsolationLevel.SERIALIZABLE) {
            val address = db.select(t).first()
            db.delete(address)
        }
        db.transaction.required {
            val address = db.select(t).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun required_required() {
        val t = template<Address>("select * from address where address_id = 15")
        db.transaction.required {
            val address = db.select(t).first()
            db.delete(address)
            required {
                val address2 = db.select<Address>(t).firstOrNull()
                assertNull(address2)
            }
        }
        db.transaction.required {
            val address = db.select(t).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun requiresNew() {
        val t = template<Address>("select * from address where address_id = 15")
        db.transaction.requiresNew {
            val address = db.select<Address>(t).first()
            db.delete(address)
            val address2 = db.select<Address>(t).firstOrNull()
            assertNull(address2)
        }
        db.transaction.required {
            val address = db.select<Address>(t).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun required_requiresNew() {
        val t = template<Address>("select * from address where address_id = 15")
        db.transaction.required {
            val address = db.select<Address>(t).first()
            db.delete(address)
            requiresNew {
                val address2 = db.select<Address>(t).firstOrNull()
                assertNotNull(address2)
            }
        }
        db.transaction.required {
            val address = db.select<Address>(t).firstOrNull()
            assertNull(address)
        }
    }

    @Test
    fun invoke() {
        val sql = template<Address>("select * from address where address_id = 15")
        db.transaction {
            val address = db.select(sql).first()
            db.delete(address)
        }
        db.transaction {
            val address = db.select(sql).firstOrNull()
            assertNull(address)
        }
    }

    class DataTypeEntityMetaResolver : EntityMetaResolver {
        override fun <T : Any> resolve(kClass: KClass<T>): EntityMeta<T> {
            val id = kClass.memberProperties.first { it.name == "id" }.let { IdMeta.Assign(it.name) }
            return EntityMeta(kClass, idList = listOf(id))
        }
    }

    val dataTypeConfig = object : DbConfig() {
        override val dataSource = config.dataSource
        override val dialect = config.dialect
        override val entityMetaResolver = DataTypeEntityMetaResolver()
    }

    @Nested
    inner class DataType {

        @Test
        fun array() {
            data class ArrayTest(val id: Int, val value: java.sql.Array)

            val db = Db(dataTypeConfig)
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
            data class BlobTest(val id: Int, val value: Blob)

            val db = Db(dataTypeConfig)
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
            data class ClobTest(val id: Int, val value: Clob)

            val db = Db(dataTypeConfig)
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
            data class NClobTest(val id: Int, val value: NClob)

            val db = Db(dataTypeConfig)
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
            data class SqlXmlTest(val id: Int, val value: SQLXML)

            val db = Db(dataTypeConfig)
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
