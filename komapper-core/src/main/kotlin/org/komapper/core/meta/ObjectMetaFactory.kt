package org.komapper.core.meta

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import org.komapper.core.value.Value

interface ObjectMetaFactory {
    fun <T : Any> get(clazz: KClass<T>): ObjectMeta
    fun toMap(obj: Any?): Map<String, Value>
}

open class DefaultObjectMetaFactory : ObjectMetaFactory {

    private val cache = ConcurrentHashMap<KClass<*>, ObjectMeta>()

    override fun <T : Any> get(clazz: KClass<T>): ObjectMeta = cache.computeIfAbsent(clazz) { c ->
        val props: Collection<KProperty1<*, *>> = c.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        ObjectMeta(props)
    }

    override fun toMap(obj: Any?): Map<String, Value> = if (obj == null) {
        emptyMap()
    } else {
        val meta = get(obj::class)
        meta.toMap(obj)
    }
}
