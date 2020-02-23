package org.komapper.core.logging

import org.komapper.core.sql.Stmt

interface Logger {
    fun logTxMessage(message: () -> String)
    fun logStmt(stmt: Stmt)
}

open class StdoutLogger : Logger {
    override fun logTxMessage(message: () -> String) = println(message())
    override fun logStmt(stmt: Stmt) {
        stmt.log?.let { println(it) }
    }
}
