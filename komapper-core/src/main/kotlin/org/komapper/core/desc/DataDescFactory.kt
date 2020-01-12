package org.komapper.core.desc

import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure
import org.komapper.core.metadata.MetadataResolver

interface DataDescFactory {
    fun <T : Any> create(
        kClass: KClass<T>,
        isMarkedNullable: Boolean,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): DataDesc<T>
}

open class DefaultDataDescFactory(
    private val metadataResolver: MetadataResolver,
    private val propDescFactory: PropDescFactory
) : DataDescFactory {
    override fun <T : Any> create(
        kClass: KClass<T>,
        isMarkedNullable: Boolean,
        hierarchy: List<KClass<*>>,
        receiverResolver: (Any) -> Any?
    ): DataDesc<T> {
        require(kClass.isData) { "The kClass must be a data class." }
        val metadata = metadataResolver.resolve(kClass)
        val constructor = kClass.primaryConstructor ?: error("The kClazz has no primary constructor.")
        val copy = kClass.memberFunctions.find {
            it.name == "copy" && it.returnType.jvmErasure == kClass && it.parameters.size == constructor.parameters.size + 1
        } ?: error("The kClazz doesn't have a copy function.")
        val propDescList = constructor.parameters
            .zip(copy.parameters.subList(1, copy.parameters.size))
            .map { (constructorParam, copyParam) ->
                check(constructorParam.type == copyParam.type) {
                    "${constructorParam.type} is not equal to ${copyParam.type}"
                }
                val prop = kClass.memberProperties.find { it.name == constructorParam.name!! }
                    ?: error("The property \"${constructorParam.name}\" is not found.")
                check(constructorParam.type == prop.returnType) {
                    "${constructorParam.type} is not equal to ${prop.returnType}"
                }
                propDescFactory.create(
                    metadata,
                    constructorParam,
                    copyParam,
                    prop,
                    this,
                    hierarchy,
                    receiverResolver
                )
            }
        return DataDesc(metadata, constructor, copy, propDescList, isMarkedNullable)
    }
}
