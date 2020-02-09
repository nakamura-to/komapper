package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.sql.template

@ExtendWith(Env::class)
internal class SelectOneColumnTest(private val db: Db) {

    @Test
    fun test() {
        val t = template<String>("select street from address")
        val list = db.selectOneColumn(t)
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals("STREET 1", list[0])
    }

    @Test
    fun sequence() {
        val t = template<String?>("select street from address")
        val list = db.selectOneColumn(t) { it.toList() }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals("STREET 1", list[0])
    }
}
