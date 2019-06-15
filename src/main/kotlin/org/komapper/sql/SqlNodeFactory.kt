package org.komapper.sql

import java.util.concurrent.ConcurrentHashMap

interface SqlNodeFactory {
    fun get(template: CharSequence): SqlNode
    fun clear()
}

open class CacheSqlNodeFactory: SqlNodeFactory {

    private val cache = ConcurrentHashMap<String, SqlNode>()

    override fun get(template: CharSequence): SqlNode {
        return cache.computeIfAbsent(template.toString()) { SqlParser(it).parse()}
    }

    override fun clear() {
        cache.clear()
    }
}

open class NoCacheSqlNodeFactory: SqlNodeFactory {

    override fun get(template: CharSequence): SqlNode {
        return SqlParser(template.toString()).parse()
    }

    override fun clear() {
    }
}
