package org.komapper.core.desc

import kotlin.reflect.KClass
import org.komapper.core.metadata.MetadataResolver

interface EmbeddedDescFactory {
    fun create(
        clazz: KClass<*>,
        propDescFactory: PropDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): EmbeddedDesc
}

open class DefaultEmbeddedDescFactory(
    private val metadataResolver: MetadataResolver
) : EmbeddedDescFactory {

    override fun create(
        clazz: KClass<*>,
        propDescFactory: PropDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): EmbeddedDesc {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        val result = kotlin.runCatching {
            metadataResolver.resolve(clazz)
        }
        val metadata = result.getOrNull()
        val desc = DataClassDesc(clazz, metadata, propDescFactory, hierarchy + clazz, receiverResolver)
        return EmbeddedDesc(clazz, desc.cons, desc.copy, desc.propMetaList)
    }
}
