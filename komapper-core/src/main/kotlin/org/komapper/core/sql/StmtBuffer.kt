package org.komapper.core.sql

import kotlin.reflect.KClass
import org.komapper.core.value.Value

class StmtBuffer(
    val formatter: (Any?, KClass<*>) -> String,
    capacity: Int = 200
) {
    val sql = StringBuilder(capacity)
    val log = StringBuilder(capacity)
    val values = ArrayList<Value>()

    fun append(s: CharSequence): StmtBuffer {
        sql.append(s)
        log.append(s)
        return this
    }

    fun append(stmt: Stmt): StmtBuffer {
        this.sql.append(stmt.sql)
        values.addAll(stmt.values)
        log.append(stmt.log)
        return this
    }

    fun bind(value: Value): StmtBuffer {
        sql.append("?")
        log.append(formatter(value.obj, value.type))
        values.add(value)
        return this
    }

    fun cutBack(length: Int): StmtBuffer {
        sql.setLength(sql.length - length)
        log.setLength(log.length - length)
        return this
    }

    fun toStmt() = Stmt(sql.toString(), values, log.toString())

    override fun toString() = sql.toString()
}
