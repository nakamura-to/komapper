package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.sql.template

@ExtendWith(Env::class)
class SelectThreeColumnsTest(private val db: Db) {

    @Test
    fun test() {
        val t = template<Triple<Int, String, Int>>("select address_id, street, version from address")
        val list = db.queryThreeColumns(t)
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(15, list[14].first)
        Assertions.assertEquals("STREET 15", list[14].second)
        Assertions.assertEquals(1, list[0].third)
    }

    @Test
    fun sequence() {
        val t = template<Triple<Int, String?, Int>>("select address_id, street, version from address")
        val list = db.queryThreeColumns(t) { it.toList() }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(15, list[14].first)
        Assertions.assertEquals("STREET 15", list[14].second)
        Assertions.assertEquals(1, list[14].third)
    }
}
