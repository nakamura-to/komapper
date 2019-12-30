package org.komapper.core.desc

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EmbeddedDesc<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val propDescList: List<PropDesc<T, *>>
) {

    fun new(leaves: Map<PropDesc<*, *>, Any?>): T {
        val args = propDescList.map { it.consParam to it.new(leaves) }.toMap()
        return cons.callBy(args)
    }

    fun copy(embedded: T, predicate: (PropDesc<*, *>) -> Boolean, block: (PropDesc<*, *>, () -> Any?) -> Any?): Any? {
        val valueArgs = propDescList.mapNotNull { it.copy(embedded, predicate, block) }.toMap()
        return if (valueArgs.isEmpty()) {
            null
        } else {
            val receiverArg = copy.parameters[0] to embedded
            copy.callBy(mapOf(receiverArg) + valueArgs)
        }
    }

    fun getLeafPropMetaList(): List<PropDesc<*, *>> =
        propDescList.flatMap { it.getLeafPropMetaList() }
}
