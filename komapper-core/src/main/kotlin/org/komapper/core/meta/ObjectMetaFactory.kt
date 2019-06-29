package org.komapper.core.meta

import org.komapper.core.value.Value
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

interface ObjectMetaFactory {
    fun <T : Any> get(clazz: KClass<T>): ObjectMeta
    fun toMap(any: Any): Map<String, Value>
}

open class DefaultObjectMetaFactory : ObjectMetaFactory {

    private val cache = ConcurrentHashMap<KClass<*>, ObjectMeta>()

    override fun <T : Any> get(clazz: KClass<T>): ObjectMeta {
        return cache.computeIfAbsent(clazz) { c ->
            val props: Collection<KProperty1<*, *>> = c.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
            ObjectMeta(props)
        }
    }

    override fun toMap(any: Any): Map<String, Value> {
        val meta = get(any::class)
        return meta.toMap(any)
    }

}
