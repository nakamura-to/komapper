package org.komapper.core.meta

import kotlin.reflect.KClass

interface EmbeddedMetaFactory {
    fun <T : Any> create(
        clazz: KClass<T>,
        propMetaFactory: PropMetaFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): EmbeddedMeta<T>
}

open class DefaultEmbeddedMetaFactory : EmbeddedMetaFactory {

    override fun <T : Any> create(
        clazz: KClass<T>,
        propMetaFactory: PropMetaFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): EmbeddedMeta<T> {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        val meta = DataClassMeta(clazz, propMetaFactory, hierarchy + clazz, receiverResolver)
        return EmbeddedMeta(clazz, meta.cons, meta.copy, meta.propMetaList)
    }
}
