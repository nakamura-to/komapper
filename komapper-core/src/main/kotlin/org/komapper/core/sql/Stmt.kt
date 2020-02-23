package org.komapper.core.sql

import org.komapper.core.value.Value

data class Stmt(val sql: String, val values: List<Value>, val log: String?)
