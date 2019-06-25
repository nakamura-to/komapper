package org.komapper.it

import org.junit.jupiter.api.Test
import org.komapper.Db
import org.komapper.DbConfig
import org.komapper.jdbc.PostgreSqlDialect
import org.komapper.jdbc.SimpleDataSource

class FirstTest {

    val config = DbConfig(
        dataSource = SimpleDataSource(url = "jdbc:postgresql://127.0.0.1/", user = "postgres"),
        dialect = PostgreSqlDialect()
    )

    @Test
    fun test() {
        val db = Db(config)
        val result = db.query<Address> {
            where {
                Address::addressId eq 1
            }
        }
        println(result)
    }
}
