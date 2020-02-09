package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.sql.template

@ExtendWith(Env::class)
class PaginateTest(private val db: Db) {

    @Test
    fun test() {
        val t = template<Address>("select * from address")
        val (list, count) = db.paginate<Address>(t, limit = 3, offset = 5)
        Assertions.assertEquals(3, list.size)
        Assertions.assertEquals(
            Address(
                6,
                "STREET 6",
                1
            ), list[0]
        )
        Assertions.assertEquals(
            Address(
                7,
                "STREET 7",
                1
            ), list[1]
        )
        Assertions.assertEquals(
            Address(
                8,
                "STREET 8",
                1
            ), list[2]
        )
        Assertions.assertEquals(15, count)
    }
}
