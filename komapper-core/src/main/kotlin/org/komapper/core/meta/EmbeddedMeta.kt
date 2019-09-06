package org.komapper.core.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EmbeddedMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val propMetaList: List<PropMeta<T, *>>
) {

    fun new(leaves: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leaves) }.toMap()
        return cons.callBy(args)
    }

    fun copy(embedded: T, predicate: (PropMeta<*, *>) -> Boolean, block: (PropMeta<*, *>, () -> Any?) -> Any?): Any? {
        val valueArgs = propMetaList.mapNotNull { it.copy(embedded, predicate, block) }.toMap()
        return if (valueArgs.isEmpty()) {
            null
        } else {
            val receiverArg = copy.parameters[0] to embedded
            copy.callBy(mapOf(receiverArg) + valueArgs)
        }
    }

    fun getLeafPropMetaList(): List<PropMeta<*, *>> =
        propMetaList.flatMap { it.getLeafPropMetaList() }
}
