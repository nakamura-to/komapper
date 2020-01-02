package org.komapper.core.desc

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import org.komapper.core.value.Value

interface ObjectDescFactory {
    fun <T : Any> get(kClass: KClass<T>): ObjectDesc
    fun toMap(obj: Any?): Map<String, Value>
}

open class DefaultObjectDescFactory : ObjectDescFactory {

    private val cache = ConcurrentHashMap<KClass<*>, ObjectDesc>()

    override fun <T : Any> get(kClass: KClass<T>): ObjectDesc = cache.computeIfAbsent(kClass) { c ->
        val props: Collection<KProperty1<*, *>> = c.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        ObjectDesc(props)
    }

    override fun toMap(obj: Any?): Map<String, Value> = if (obj == null) {
        emptyMap()
    } else {
        val meta = get(obj::class)
        meta.toMap(obj)
    }
}
