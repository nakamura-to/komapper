package org.komapper.core.desc

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import org.komapper.core.Table

interface EntityDescFactory {
    fun <T : Any> get(clazz: KClass<T>): EntityDesc<T>
}

open class DefaultEntityDescFactory(
    private val quote: (String) -> String,
    private val namingStrategy: NamingStrategy,
    private val propDescFactory: PropDescFactory
) : EntityDescFactory {

    private val cache = ConcurrentHashMap<KClass<*>, EntityDesc<*>>()

    override fun <T : Any> get(clazz: KClass<T>): EntityDesc<T> {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(clazz) { create(it) } as EntityDesc<T>
    }

    protected open fun <T : Any> create(clazz: KClass<T>): EntityDesc<T> {
        val meta = DataClassDesc(clazz, propDescFactory, listOf(clazz)) { it }
        val table = clazz.findAnnotation<Table>()
        val name = table?.name ?: namingStrategy.fromKotlinToDb(clazz.simpleName!!)
        val tableName = if (table?.quote == true) quote(name) else name
        return EntityDesc(clazz, meta.cons, meta.copy, meta.propMetaList, tableName)
    }
}
