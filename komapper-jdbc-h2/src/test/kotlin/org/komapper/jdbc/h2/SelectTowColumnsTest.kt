package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.sql.template

@ExtendWith(Env::class)
internal class SelectTowColumnsTest(private val db: Db) {

    @Test
    fun test() {
        val t = template<Pair<Int, String>>("select address_id, street from address")
        val list = db.selectTwoColumns(t)
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(1, list[0].first)
        Assertions.assertEquals("STREET 1", list[0].second)
    }

    @Test
    fun sequence() {
        val t = template<Pair<Int, String?>>("select address_id, street from address")
        val list = db.selectTwoColumns(t) { it.toList() }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(1, list[0].first)
        Assertions.assertEquals("STREET 1", list[0].second)
    }
}
