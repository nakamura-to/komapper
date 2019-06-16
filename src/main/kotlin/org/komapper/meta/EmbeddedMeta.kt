package org.komapper.meta

import org.komapper.Value
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EmbeddedMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val propMetaList: List<PropMeta<T, *>>
) {

    fun new(leafs: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leafs) }.toMap()
        return cons.callBy(args)
    }

    fun copy(embedded: T, predicate: (PropMeta<*, *>) -> Boolean, block: (PropMeta<*, *>, Any?) -> Any?): Any? {
        val valueArgs = propMetaList.mapNotNull { it.copy(embedded, predicate, block) }.toMap()
        return if (valueArgs.isEmpty()) {
            null
        } else {
            val receiverArg = copy.parameters[0] to embedded
            copy.callBy(mapOf(receiverArg) + valueArgs)
        }
    }

    fun getValues(embedded: T, predicate: (PropMeta<*, *>) -> Boolean): List<Value> =
        propMetaList.flatMap { it.getValues(embedded, predicate) }

    fun getColumnNames(predicate: (PropMeta<*, *>) -> Boolean): List<String> =
        propMetaList.flatMap { it.getColumnNames(predicate) }

    fun getLeafPropMetaList(): List<PropMeta<*, *>> =
        propMetaList.flatMap { it.getLeafPropMetaList() }

}
