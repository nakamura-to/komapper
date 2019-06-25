package org.komapper.it

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db
import org.komapper.DbConfig
import org.komapper.jdbc.PostgreSqlDialect
import org.komapper.jdbc.SimpleDataSource

@ExtendWith(Env::class)
class FirstTest {

    @Test
    fun test(db: Db) {
        db.transaction {
            val result = db.query<Address> {
                where {
                    Address::addressId eq 1
                }
            }
            println(result)
        }
    }
}
