package koma.sql

import koma.Value

class SqlBuffer(capacity: Int = 200) {
    val sql = StringBuilder(capacity)
    val log = StringBuilder(capacity)
    val values = ArrayList<Value>()

    fun append(s: CharSequence) {
        sql.append(s)
        log.append(s)
    }

    fun bind(value: Value) {
        sql.append("?")
        log.append(toText(value))
        values.add(value)
    }

    fun cutBack(length: Int) {
        sql.setLength(sql.length - length)
        log.setLength(log.length - length)
    }

    fun toSql(): Sql {
        return Sql(sql.toString(), values, log.toString())
    }

    private fun toText(value: Value): String {
        val (obj) = value
        return if (obj is CharSequence) "'$obj'" else obj.toString()
    }

}
