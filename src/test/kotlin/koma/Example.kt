package koma

import koma.jdbc.SimpleDataSource

data class Address(
    @Id
    @SequenceGenerator(name = "ADDRESS_SEQ", incrementBy = 100)
    @Column(name = "address_id")
    val id: Int = 0,
    val street: String,
    @Version
    val version: Int = 0
)

fun main() {
    val db = Db(
        DbConfig(
            dataSource = SimpleDataSource("jdbc:h2:mem:koma;DB_CLOSE_DELAY=-1"),
            dialect = H2Dialect(),
            logger = { println(it()) },
            useTransaction = true
        )
    )

    // set up data
    db.transaction.required {
        db.execute(
            """
            CREATE SEQUENCE ADDRESS_SEQ START WITH 1 INCREMENT BY 100;
            CREATE TABLE ADDRESS(ADDRESS_ID INTEGER NOT NULL PRIMARY KEY, STREET VARCHAR(20) UNIQUE, VERSION INTEGER);
            INSERT INTO ADDRESS (ADDRESS_ID, STREET, VERSION) VALUES(1, 'street A', 1)
            """.trimIndent()
        )
    }

    // query
    db.transaction.required {
        val addressA = db.findById<Address>(1)
        println(addressA) // Address(id=1, street=street A, version=1)

        addressA?.let {
            val addressB = db.update(addressA.copy(street = "street B"))
            println(addressB) // Address(id=1, street=street B, version=2)
        }

        val addressB = db.select<Address>("select * from address where /*street*/'test'", object {
            val street = "street B"
        }).first()
        println(addressB) // Address(id=1, street=street B, version=2)

        db.delete(addressB)

        val addressList = db.select<Address>("select * from address")
        println(addressList.size) // 0

        val addressC = db.insert<Address>(Address(street = "street C"))
        println(addressC) // Address(id=1, street=street C, version=0)
    }
}
