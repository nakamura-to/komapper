package org.komapper.core

enum class LogKind {
    SQL,
    TRANSACTION
}

interface Logger {
    fun log(kind: LogKind, lazyMessage: () -> String)
}

class StdoutLogger : Logger {
    override fun log(kind: LogKind, lazyMessage: () -> String) = println(lazyMessage())
}
