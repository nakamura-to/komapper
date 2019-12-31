package org.komapper.core.desc

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

class PropDesc(
    val type: KClass<*>,
    val consParam: KParameter,
    val copyParam: KParameter,
    val prop: KProperty1<*, *>,
    val deepGetter: (Any) -> Any?,
    val kind: PropKind,
    val columnLabel: String,
    val columnName: String
) {

    @Suppress("UNCHECKED_CAST")
    fun new(leafValues: Map<PropDesc, Any?>): Any? = when (kind) {
        is PropKind.Embedded -> kind.embeddedDesc.new(leafValues)
        else -> leafValues[this]
    }

    fun copy(
        owner: Any,
        predicate: (PropDesc) -> Boolean,
        block: (PropDesc, () -> Any?) -> Any?
    ): Pair<KParameter, Any?>? = when (kind) {
        is PropKind.Embedded -> {
            prop.call(owner)?.let { embedded ->
                kind.embeddedDesc.copy(embedded, predicate, block)?.let { newEmbedded ->
                    copyParam to newEmbedded
                }
            }
        }
        else -> {
            if (predicate(this)) {
                val newValue = block(this) { prop.call(owner) }
                copyParam to newValue
            } else {
                null
            }
        }
    }

    fun getLeafPropMetaList(): List<PropDesc> = when (kind) {
        is PropKind.Embedded -> kind.embeddedDesc.getLeafPropMetaList()
        else -> listOf(this)
    }

    fun next(key: String, callNextValue: (String) -> Long): Any? = when (kind) {
        is PropKind.Id.Sequence -> kind.next(key, callNextValue)
        else -> error("illegal invocation: $kind")
    }

    @Suppress("UNCHECKED_CAST")
    fun inc(value: Any): Any? = when (kind) {
        is PropKind.Version -> kind.inc(value)
        else -> error("illegal invocation: $kind")
    }

    fun now(): Any? = when (kind) {
        is PropKind.CreatedAt -> kind.now()
        is PropKind.UpdatedAt -> kind.now()
        else -> error("illegal invocation: $kind")
    }
}
