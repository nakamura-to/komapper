package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db

@ExtendWith(Env::class)
internal class ExecuteUpdateTest(private val db: Db) {

    @Test
    fun test() {
        val count =
            db.executeUpdate("update address set street = /*street*/'' where address_id = /*id*/0", object {
                val id = 15
                val street = "NY street"
            })
        Assertions.assertEquals(1, count)
        val address = db.query<Address>("select * from address where address_id = 15").firstOrNull()
        Assertions.assertEquals(
            Address(
                15,
                "NY street",
                1
            ), address
        )
    }
}
