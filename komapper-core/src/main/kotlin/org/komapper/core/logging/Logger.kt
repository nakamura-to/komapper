package org.komapper.core.logging

import org.komapper.core.sql.Sql

interface Logger {
    fun log(lazyMessage: () -> String)
    fun logSql(sql: Sql)
}

open class StdoutLogger : Logger {
    override fun log(lazyMessage: () -> String) = println(lazyMessage())
    override fun logSql(sql: Sql) {
        sql.log?.let { println(it) }
    }
}
