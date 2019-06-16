package org.komapper.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class EmbeddedMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val propMetaList: List<PropMeta<T, *>>
) {

    fun getLeafPropMetaList(): List<PropMeta<*, *>> {
        return propMetaList.flatMap {
            when (it.kind) {
                is PropKind.Embedded -> it.kind.meta.getLeafPropMetaList()
                else -> listOf(it)
            }
        }
    }

    fun new(leafs: Map<PropMeta<*, *>, Any?>): T {
        val args = propMetaList.map { it.consParam to it.new(leafs) }.toMap()
        return cons.callBy(args)
    }
}
