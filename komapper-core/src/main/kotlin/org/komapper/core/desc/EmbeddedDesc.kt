package org.komapper.core.desc

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EmbeddedDesc(
    val type: KClass<*>,
    val cons: KFunction<*>,
    val copy: KFunction<*>,
    val propDescList: List<PropDesc>
) {

    fun new(leaves: Map<PropDesc, Any?>): Any? {
        val args = propDescList.map { it.consParam to it.new(leaves) }.toMap()
        return cons.callBy(args)
    }

    fun copy(embedded: Any, predicate: (PropDesc) -> Boolean, block: (PropDesc, () -> Any?) -> Any?): Any? {
        val valueArgs = propDescList.mapNotNull { it.copy(embedded, predicate, block) }.toMap()
        return if (valueArgs.isEmpty()) {
            null
        } else {
            val receiverArg = copy.parameters[0] to embedded
            copy.callBy(mapOf(receiverArg) + valueArgs)
        }
    }

    fun getLeafPropMetaList(): List<PropDesc> =
        propDescList.flatMap { it.getLeafPropMetaList() }
}
