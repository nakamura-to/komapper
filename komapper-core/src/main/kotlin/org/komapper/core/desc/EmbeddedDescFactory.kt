package org.komapper.core.desc

import kotlin.reflect.KClass

interface EmbeddedDescFactory {
    fun <T : Any> create(
        clazz: KClass<T>,
        propDescFactory: PropDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): EmbeddedDesc<T>
}

open class DefaultEmbeddedMetaFactory : EmbeddedDescFactory {

    override fun <T : Any> create(
        clazz: KClass<T>,
        propDescFactory: PropDescFactory,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): EmbeddedDesc<T> {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        val meta = DataClassDesc(clazz, propDescFactory, hierarchy + clazz, receiverResolver)
        return EmbeddedDesc(clazz, meta.cons, meta.copy, meta.propMetaList)
    }
}
