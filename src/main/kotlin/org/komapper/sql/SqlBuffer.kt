package org.komapper.sql

import org.komapper.Value
import kotlin.reflect.KClass

class SqlBuffer(
    val formatter: (Any?, KClass<*>) -> String,
    capacity: Int = 200
) {
    val sql = StringBuilder(capacity)
    val log = StringBuilder(capacity)
    val values = ArrayList<Value>()

    fun append(s: CharSequence) {
        sql.append(s)
        log.append(s)
    }

    fun bind(value: Value) {
        sql.append("?")
        log.append(formatter(value.first, value.second))
        values.add(value)
    }

    fun cutBack(length: Int) {
        sql.setLength(sql.length - length)
        log.setLength(log.length - length)
    }

    fun toSql(): Sql {
        return Sql(sql.toString(), values, log.toString())
    }
}
