package org.komapper.core.sql

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import org.komapper.core.value.Value

interface AnyDescFactory {
    fun <T : Any> get(kClass: KClass<T>): AnyDesc
    fun clear()
    fun toMap(any: Any?): Map<String, Value> {
        return if (any == null) {
            emptyMap()
        } else {
            val desc = get(any::class)
            desc.toMap(any)
        }
    }
}

open class CacheAnyDescFactory : AnyDescFactory {
    private val cache = ConcurrentHashMap<KClass<*>, AnyDesc>()

    override fun <T : Any> get(kClass: KClass<T>): AnyDesc = cache.computeIfAbsent(kClass) { k ->
        val props = k.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        AnyDesc(props)
    }

    override fun clear() = cache.clear()
}

open class NoCacheAnyDescFactory : AnyDescFactory {
    override fun <T : Any> get(kClass: KClass<T>): AnyDesc {
        val props = kClass.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        return AnyDesc(props)
    }

    override fun clear() = Unit
}
