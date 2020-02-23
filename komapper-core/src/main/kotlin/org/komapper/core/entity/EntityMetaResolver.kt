package org.komapper.core.entity

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface EntityMetaResolver {
    fun <T : Any> resolve(kClass: KClass<T>): EntityMeta<T>
}

open class DefaultEntityMetaResolver(metadata: Map<KClass<*>, EntityMeta<*>>) :
    EntityMetaResolver {
    private val cache = ConcurrentHashMap(metadata)

    override fun <T : Any> resolve(kClass: KClass<T>): EntityMeta<T> {
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(kClass) { EntityMeta(it) } as EntityMeta<T>
    }
}
