package example

import java.time.LocalDateTime
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.jdbc.H2Dialect
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.metadata.EntityMetadata
import org.komapper.core.metadata.SequenceGenerator

data class Address(
    val id: Int = 0,
    val street: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val version: Int = 0
)

object AddressMetadata : EntityMetadata<Address>({
    id(Address::id, SequenceGenerator("ADDRESS_SEQ", 100))
    createdAt(Address::createdAt)
    updatedAt(Address::updatedAt)
    version(Address::version)
    table {
        column(Address::id, "address_id")
    }
})

fun main() {
    val db = Db(
        object : DbConfig() {
            override val dataSource = SimpleDataSource("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
            override val dialect = H2Dialect()
        }
    )

    // set up schema
    db.transaction {
        db.execute(
            """
            CREATE SEQUENCE ADDRESS_SEQ START WITH 1 INCREMENT BY 100;
            CREATE TABLE ADDRESS(
                ADDRESS_ID INTEGER NOT NULL PRIMARY KEY,
                STREET VARCHAR(20) UNIQUE,
                CREATED_AT TIMESTAMP,
                UPDATED_AT TIMESTAMP,
                VERSION INTEGER
            );
            """.trimIndent()
        )
    }

    db.transaction {
        // insert into address (address_id, street, created_at, updated_at, version) values (1, 'street A', '2019-09-07T17:24:25.729', null, 0)
        val addressA = db.insert(Address(street = "street A"))

        // Address(id=1, street=street A, createdAt=2019-09-07T17:24:25.729, updatedAt=null, version=0)
        println(addressA)

        // select address_id, street, created_at, updated_at, version from address where address_id = 1
        val foundA = db.findById<Address>(1)

        assert(addressA == foundA)

        // update address set street = 'street B', created_at = '2019-09-07T17:24:25.729', updated_at = '2019-09-07T17:24:25.816', version = 1 where address_id = 1 and version = 0
        val addressB = db.update(addressA.copy(street = "street B"))

        // Address(id=1, street=street B, createdAt=2019-09-07T17:24:25.729, updatedAt=2019-09-07T17:24:25.816, version=1)
        println(addressB)

        // select t0_.address_id, t0_.street, t0_.created_at, t0_.updated_at, t0_.version from address t0_ where t0_.street = 'street B'
        val foundB1 = db.select<Address> {
            where {
                Address::street eq "street B"
            }
        }.first()

        // select address_id, street, created_at, updated_at, version from Address where street = 'street B'
        val foundB2 = db.query<Address>(
            "select /*%expand*/* from Address where street = /*street*/'test'",
            object {
                val street = "street B"
            }
        ).first()

        assert(addressB == foundB1 && foundB1 == foundB2)

        // delete from address where address_id = 1 and version = 1
        db.delete(addressB)

        // select t0_.address_id, t0_.street, t0_.created_at, t0_.updated_at, t0_.version from address t0_
        val addressList = db.select<Address>()

        assert(addressList.isEmpty())
    }
}
