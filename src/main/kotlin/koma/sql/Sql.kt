package koma.sql

import koma.Value

data class Sql(val text: String, val values: List<Value>, val log: String)
