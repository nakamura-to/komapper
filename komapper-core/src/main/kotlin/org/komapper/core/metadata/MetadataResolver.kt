package org.komapper.core.metadata

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface MetadataResolver {
    fun <T : Any> resolve(kClass: KClass<T>): Metadata<T>
}

open class CollectedMetadataResolver(collection: Collection<Metadata<*>>) : MetadataResolver {
    constructor(vararg metadata: Metadata<*>) : this(setOf(*metadata))

    private val cache = ConcurrentHashMap(collection.associateBy { it.kClass })

    override fun <T : Any> resolve(kClass: KClass<T>): Metadata<T> {
        @Suppress("UNCHECKED_CAST")
        return cache.computeIfAbsent(kClass) { Metadata(it) } as Metadata<T>
    }
}
