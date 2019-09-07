package example

import java.time.LocalDateTime
import org.komapper.core.Column
import org.komapper.core.CreatedAt
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.Id
import org.komapper.core.SequenceGenerator
import org.komapper.core.UpdatedAt
import org.komapper.core.Version
import org.komapper.core.jdbc.H2Dialect
import org.komapper.core.jdbc.SimpleDataSource

data class Address(
    @Id
    @SequenceGenerator(name = "ADDRESS_SEQ", incrementBy = 100)
    @Column(name = "address_id")
    val id: Int = 0,
    val street: String,
    @CreatedAt
    val createdAt: LocalDateTime? = null,
    @UpdatedAt
    val updatedAt: LocalDateTime? = null,
    @Version
    val version: Int = 0
)

fun main() {
    val db = Db(
        DbConfig(
            dataSource = SimpleDataSource("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1"),
            dialect = H2Dialect(),
            useTransaction = true
        )
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

        // true
        println(addressA == foundA)

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

        // true
        println(addressB == foundB1 && foundB1 == foundB2)

        // delete from address where address_id = 1 and version = 1
        db.delete(addressB)

        // select t0_.address_id, t0_.street, t0_.created_at, t0_.updated_at, t0_.version from address t0_
        val addressList = db.select<Address>()

        // 0
        println(addressList.size)
    }
}
