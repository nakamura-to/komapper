package org.komapper.core

interface Logger {
    operator fun invoke(lazyMessage: () -> String)
}

class StdoutLogger : Logger {
    override operator fun invoke(lazyMessage: () -> String) = println(lazyMessage())
}
