package example

import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.criteria.select
import org.komapper.core.entity.DefaultEntityMetaResolver
import org.komapper.core.entity.SequenceGenerator
import org.komapper.core.entity.entities
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.sql.template
import org.komapper.jdbc.h2.H2Dialect

// entity
data class Address(
    val id: Int = 0,
    val street: String,
    val version: Int = 0
)

// entity metadata
val metadata = entities {
    entity<Address> {
        id(Address::id, SequenceGenerator("ADDRESS_SEQ", 100))
        version(Address::version)
        table {
            column(Address::id, "address_id")
        }
    }
}

fun main() {
    // create Db instance
    val db = Db(
        // configuration
        object : DbConfig() {
            // dataSource for H2
            override val dataSource = SimpleDataSource("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
            // dialect for H2
            override val dialect = H2Dialect()
            // register entity metadata
            override val entityMetaResolver = DefaultEntityMetaResolver(metadata)
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
                VERSION INTEGER
            );
            """.trimIndent()
        )
    }

    // execute simple CRUD operations as a transaction
    db.transaction {
        // CREATE
        val addressA = db.insert(Address(street = "street A"))
        println(addressA)

        // READ: select by identifier
        val foundA = db.findById<Address>(1)
        assert(addressA == foundA)

        // UPDATE
        val addressB = db.update(addressA.copy(street = "street B"))
        println(addressB)

        // READ: select by criteria query
        val criteriaQuery = select<Address> {
            where {
                eq(Address::street, "street B")
            }
        }
        val foundB1 = db.select(criteriaQuery).first()
        assert(addressB == foundB1)

        // READ: select by template query
        val templateQuery = template<Address>(
            "select /*%expand*/* from Address where street = /*street*/'test'",
            object {
                val street = "street B"
            }
        )
        val foundB2 = db.select(templateQuery).first()
        assert(addressB == foundB2)

        // DELETE
        db.delete(addressB)

        // READ: select by criteria query
        val addressList = db.select<Address>()
        assert(addressList.isEmpty())
    }
}
