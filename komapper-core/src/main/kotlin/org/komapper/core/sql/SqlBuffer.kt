package org.komapper.core.sql

import kotlin.reflect.KClass
import org.komapper.core.value.Value

class SqlBuffer(
    val formatter: (Any?, KClass<*>) -> String,
    capacity: Int = 200
) {
    val sql = StringBuilder(capacity)
    val log = StringBuilder(capacity)
    val values = ArrayList<Value>()

    fun append(s: CharSequence): SqlBuffer {
        sql.append(s)
        log.append(s)
        return this
    }

    fun bind(value: Value): SqlBuffer {
        sql.append("?")
        log.append(formatter(value.obj, value.type))
        values.add(value)
        return this
    }

    fun cutBack(length: Int): SqlBuffer {
        sql.setLength(sql.length - length)
        log.setLength(log.length - length)
        return this
    }

    fun toSql(): Sql = Sql(sql.toString(), values, log.toString())
}
