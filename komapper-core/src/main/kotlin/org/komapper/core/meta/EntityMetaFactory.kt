package org.komapper.core.meta

import org.komapper.core.Table
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface EntityMetaFactory {
    fun <T : Any> get(clazz: KClass<T>): EntityMeta<T>
}

open class DefaultEntityMetaFactory(
    private val quote: (String) -> String,
    private val namingStrategy: NamingStrategy,
    private val propMetaFactory: PropMetaFactory
) : EntityMetaFactory {

    private val cache = ConcurrentHashMap<KClass<*>, EntityMeta<*>>()

    override fun <T : Any> get(clazz: KClass<T>): EntityMeta<T> {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(clazz) { create(it) } as EntityMeta<T>
    }

    protected open fun <T : Any> create(clazz: KClass<T>): EntityMeta<T> {
        val meta = DataClassMeta(clazz, propMetaFactory, listOf(clazz)) { it }
        val table = clazz.findAnnotation<Table>()
        val name = table?.name ?: namingStrategy.fromKotlinToDb(clazz.simpleName!!)
        val tableName = if (table?.quote == true) quote(name) else name
        return EntityMeta(clazz, meta.cons, meta.copy, meta.propMetaList, tableName)
    }

}
