package org.komapper.core.desc

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface EntityDescFactory {
    fun <T : Any> get(clazz: KClass<T>): EntityDesc<T>
}

open class DefaultEntityDescFactory(
    private val dataDescFactory: DataDescFactory,
    private val quote: (String) -> String,
    private val namingStrategy: NamingStrategy
) : EntityDescFactory {

    private val cache = ConcurrentHashMap<KClass<*>, EntityDesc<*>>()

    override fun <T : Any> get(clazz: KClass<T>): EntityDesc<T> {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(clazz) { create(it) } as EntityDesc<T>
    }

    protected open fun <T : Any> create(clazz: KClass<T>): EntityDesc<T> {
        val desc = dataDescFactory.create(clazz, false, listOf(clazz)) { it }
        val table = desc.metadata.table
        val name = table.name ?: namingStrategy.fromKotlinToDb(clazz.simpleName!!)
        val tableName = if (table.quote) quote(name) else name
        return EntityDesc(desc, tableName)
    }
}
