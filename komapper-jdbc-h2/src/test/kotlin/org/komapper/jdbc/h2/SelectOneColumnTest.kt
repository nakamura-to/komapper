package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db

@ExtendWith(Env::class)
internal class SelectOneColumnTest(private val db: Db) {

    @Test
    fun test() {
        val list = db.queryOneColumn<String>("select street from address")
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals("STREET 1", list[0])
    }

    @Test
    fun sequence() {
        val list = db.queryOneColumn<String?, List<String?>>("select street from address") {
            it.toList()
        }
        Assertions.assertEquals(15, list.size)
        Assertions.assertEquals("STREET 1", list[0])
    }
}
