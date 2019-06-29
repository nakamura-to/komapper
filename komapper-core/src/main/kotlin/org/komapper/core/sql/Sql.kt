package org.komapper.core.sql

import org.komapper.core.value.Value

data class Sql(val text: String, val values: List<Value>, val log: String?)
