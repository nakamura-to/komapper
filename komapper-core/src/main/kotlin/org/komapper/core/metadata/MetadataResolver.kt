package org.komapper.core.metadata

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface MetadataResolver {
    fun <T : Any> resolve(kClass: KClass<T>): Metadata<T>
}

open class CollectedMetadataResolver(map: Map<KClass<*>, Metadata<*>>) : MetadataResolver {
    private val cache = ConcurrentHashMap(map)

    override fun <T : Any> resolve(kClass: KClass<T>): Metadata<T> {
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(kClass) { Metadata(it) } as Metadata<T>
    }
}
