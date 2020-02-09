package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.sql.template

@ExtendWith(Env::class)
internal class ExecuteTest(private val db: Db) {

    @Test
    fun test() {
        db.execute(
            """
            create table execute_table(value varchar(20));
            insert into execute_table(value) values('test');
            """.trimIndent()
        )
        val t = template<String>("select value from execute_table")
        val value = db.selectOneColumn(t).firstOrNull()
        Assertions.assertEquals("test", value)
    }
}
