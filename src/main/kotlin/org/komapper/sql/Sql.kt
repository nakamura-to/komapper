package org.komapper.sql

import org.komapper.Value

data class Sql(val text: String, val values: List<Value>, val log: String?)
