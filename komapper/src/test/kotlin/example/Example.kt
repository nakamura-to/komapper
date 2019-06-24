package example

import org.komapper.*
import org.komapper.jdbc.H2Dialect
import org.komapper.jdbc.SimpleDataSource
import java.time.LocalDateTime

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

    // query
    db.transaction {
        // insert into address (address_id, street, created_at, updated_at, version) values(1, 'street A', '2019-06-02 18:15:36.561', null, 0)
        val addressA = db.insert(Address(street = "street A"))

        // Address(id=1, street=street A, createdAt=2019-06-02T18:15:36.561, updatedAt=null, version=0)
        println(addressA)

        // select address_id, street, created_at, updated_at, version from address where address_id = 1
        val foundA = db.findById<Address>(1)

        // true
        println(addressA == foundA)

        // update address set street = 'street B', created_at = '2019-06-02 18:15:36.561', updated_at = '2019-06-02 18:15:36.601', version = 1 where address_id = 1 and version = 0
        val addressB = db.update(addressA.copy(street = "street B"))

        // Address(id=1, street=street B, createdAt=2019-06-02T18:15:36.561, updatedAt=2019-06-02T18:15:36.601, version=1)
        println(addressB)

        // select address_id, street, created_at, updated_at, version from address where street = 'street B'
        val foundB = db.query<Address> {
            where {
                Address::street eq "street B"
            }
        }.first()

        // true
        println(addressB == foundB)

        // delete from address where address_id = 1 and version = 1
        db.delete(addressB)

        // select address_id, street, created_at, updated_at, version from address
        val addressList = db.query<Address>()

        // 0
        println(addressList.size)
    }
}
