package org.komapper.meta

import org.komapper.Table
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

interface EntityMetaFactory {
    fun <T : Any> get(clazz: KClass<T>): EntityMeta<T>
}

open class DefaultEntityMetaFactory(
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
        val meta = DataClassMeta(clazz, propMetaFactory, listOf(clazz))
        val table = clazz.findAnnotation<Table>()
        val tableName = table?.name ?: namingStrategy.fromKotlinToDb(clazz.simpleName!!)
        return EntityMeta(clazz, meta.cons, meta.copy, meta.propMetaList, tableName)
    }

}
