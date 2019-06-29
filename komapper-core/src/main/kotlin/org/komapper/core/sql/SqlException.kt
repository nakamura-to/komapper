package org.komapper.core.sql

class SqlException(message: String, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
}
