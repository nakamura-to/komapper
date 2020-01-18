package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db

@ExtendWith(Env::class)
internal class SelectTowColumnsTest(private val db: Db) {

    @Test
    fun test() {
        val list = db.queryTwoColumns<Int, String>("select address_id, street from address")
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(1, list[0].first)
        Assertions.assertEquals("STREET 1", list[0].second)
    }

    @Test
    fun sequence() {
        val list = db.queryTwoColumns<Int, String?, List<Pair<Int, String?>>>(
            "select address_id, street from address"
        ) { it.toList() }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals(1, list[0].first)
        Assertions.assertEquals("STREET 1", list[0].second)
    }
}
