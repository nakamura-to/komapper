package org.komapper.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class EmbeddedMeta<T>(
    val type: KClass<*>,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
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

    fun getEmbeddedPropMetaList(): List<PropMeta<*, *>> {
        return propMetaList.flatMap {
            when (it.kind) {
                is PropKind.Embedded -> listOf(it) + it.kind.meta.getEmbeddedPropMetaList()
                else -> emptyList()
            }
        }
    }

    fun new(args: Map<KParameter, Any?>): T {
        return cons.callBy(args)
    }

    fun copy(args: Map<KParameter, Any?>): T {
        return copy.callBy(args)
    }

}
