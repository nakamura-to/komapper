package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.sql.template

@ExtendWith(Env::class)
internal class ExecuteUpdateTest(private val db: Db) {

    @Test
    fun test() {
        val updateAddress =
            template<Int>("update address set street = /*street*/'' where address_id = /*id*/0", object {
                val id = 15
                val street = "NY street"
            })
        val count = db.executeUpdate(updateAddress)
        Assertions.assertEquals(1, count)
        val selectAddress = template<Address>("select * from address where address_id = 15")
        val address = db.select(selectAddress).firstOrNull()
        Assertions.assertEquals(
            Address(
                15,
                "NY street",
                1
            ), address
        )
    }
}
