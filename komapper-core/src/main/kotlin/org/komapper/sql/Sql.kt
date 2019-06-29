package org.komapper.sql

import org.komapper.value.Value

data class Sql(val text: String, val values: List<Value>, val log: String?)
