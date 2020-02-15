package org.komapper.core.desc

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface EntityDescFactory {
    fun <T : Any> get(kClass: KClass<T>): EntityDesc<T>
}

open class DefaultEntityDescFactory(
    private val dataDescFactory: DataDescFactory,
    private val quote: (String) -> String,
    private val namingStrategy: NamingStrategy
) : EntityDescFactory {

    private val cache = ConcurrentHashMap<KClass<*>, EntityDesc<*>>()

    override fun <T : Any> get(kClass: KClass<T>): EntityDesc<T> {
        require(kClass.isData) { "The kClass must be a data class." }
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(kClass) { create(it) } as EntityDesc<T>
    }

    protected open fun <T : Any> create(kClass: KClass<T>): EntityDesc<T> {
        val desc = dataDescFactory.create(kClass, false, listOf(kClass)) { it }
        val table = desc.entityMeta.table
        val name = table.name ?: namingStrategy.fromKotlinToDb(kClass.simpleName!!)
        val tableName = if (table.quote) quote(name) else name
        return EntityDesc(desc, tableName)
    }
}
