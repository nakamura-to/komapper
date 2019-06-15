package org.komapper.meta

import org.komapper.Table
import org.komapper.jdbc.Dialect
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

interface EntityMetaFactory {
    fun <T : Any> get(clazz: KClass<T>): EntityMeta<T>
}

open class DefaultEntityMetaFactory(
    private val dialect: Dialect,
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
        val cons = clazz.primaryConstructor ?: error("The clazz has no primary constructor.")
        val copy = clazz.memberFunctions.find {
            it.name == "copy" && it.returnType.jvmErasure == clazz && it.parameters.size == cons.parameters.size + 1
        }?.let {
            @Suppress("UNCHECKED_CAST")
            it as KFunction<T>
        } ?: error("The clazz does'n have a copy function.")
        val table = clazz.findAnnotation<Table>()
        val tableName = table?.name ?: namingStrategy.fromKotlinToDb(clazz.simpleName!!)
        val propMetaList = cons.parameters
            .zip(copy.parameters.subList(1, copy.parameters.size))
            .map { (consParam, copyParam) ->
                check(consParam.type == copyParam.type) { "${consParam.type} is not equal to ${copyParam.type}" }
                val prop = clazz.memberProperties.find { it.name == consParam.name!! }
                    ?: error("The property \"${consParam.name}\" is not found.")
                check(consParam.type == prop.returnType) { "${consParam.type} is not equal to ${prop.returnType}" }
                propMetaFactory.create(consParam, copyParam, prop)
            }
        return EntityMeta(dialect, cons, copy, tableName, propMetaList)
    }

}
