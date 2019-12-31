package org.komapper.core.metadata

import kotlin.reflect.KClass

interface MetadataResolver {
    fun <T : Any> resolve(kClass: KClass<T>): Metadata<T>
}

open class DefaultMetadataResolver(private val suffix: String = "Metadata") : MetadataResolver {

    override fun <T : Any> resolve(kClass: KClass<T>): Metadata<T> {
        val metadataClassName = kClass.java.name + suffix
        val result = runCatching {
            val clazz: Class<*> = Class.forName(metadataClassName)
            @Suppress("UNCHECKED_CAST")
            val metadataClass: Class<Metadata<T>> = clazz as Class<Metadata<T>>
            metadataClass.kotlin.objectInstance ?: metadataClass.newInstance()
        }
        // TODO
        return result.getOrDefault(EntityMetadata())
    }
}
