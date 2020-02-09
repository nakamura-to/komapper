package org.komapper.core.sql

data class Template<T : Any?> internal constructor(val sql: CharSequence, val args: Any?)

fun <T : Any?> template(sql: CharSequence, args: Any? = null) = Template<T>(sql, args)
