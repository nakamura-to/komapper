package org.komapper.jdbc.h2

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.query.TemplateQuery

@ExtendWith(Env::class)
internal class ScriptQueryTest(private val db: Database) {

    @Test
    fun test() {
        @Language("sql")
        val script = """
            create table execute_table(value varchar(20));
            insert into execute_table(value) values('test');
            """
        db.script(script)

        val value = db.first {
            @Language("sql")
            val sql = "select value from execute_table"
            TemplateQuery.select(sql) {
                asString("value")
            }
        }
        Assertions.assertEquals("test", value)
    }
}
