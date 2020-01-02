package org.komapper.core.desc

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import org.komapper.core.metadata.Metadata

data class DataDesc<T : Any>(
    val kClass: KClass<T>,
    val metadata: Metadata<T>?,
    private val constructor: KFunction<*>,
    private val copy: KFunction<*>,
    private val propDescList: List<PropDesc>
) {
    fun new(leaves: Map<PropDesc, Any?>): Any? {
        val args = propDescList.map { it.constructorParam to it.new(leaves) }.toMap()
        return constructor.callBy(args)
    }

    fun copy(
        receiver: Any,
        predicate: (PropDesc) -> Boolean,
        block: (PropDesc, () -> Any?) -> Any?
    ): Any? {
        val valueArgs = propDescList.mapNotNull { it.copy(receiver, predicate, block) }.toMap()
        return if (valueArgs.isEmpty()) {
            null
        } else {
            val receiverArg = copy.parameters[0] to receiver
            copy.callBy(mapOf(receiverArg) + valueArgs)
        }
    }

    fun getLeafPropDescList(): List<PropDesc> =
        propDescList.flatMap { it.getLeafPropDescList() }
}
