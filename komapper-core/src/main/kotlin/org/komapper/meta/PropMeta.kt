package org.komapper.meta

import org.komapper.value.Value
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

class PropMeta<T, R : Any?>(
    val type: KClass<*>,
    val consParam: KParameter,
    val copyParam: KParameter,
    val prop: KProperty1<T, R>,
    val kind: PropKind<R>,
    val columnLabel: String,
    val columnName: String
) {

    @Suppress("UNCHECKED_CAST")
    fun new(leaves: Map<PropMeta<*, *>, Any?>): R = when (kind) {
        is PropKind.Embedded<R> -> kind.embeddedMeta.new(leaves)
        else -> leaves[this] as R
    }

    fun copy(
        owner: T,
        predicate: (PropMeta<*, *>) -> Boolean,
        block: (PropMeta<*, *>, () -> Any?) -> Any?
    ): Pair<KParameter, Any?>? = when (kind) {
        is PropKind.Embedded -> {
            val embedded = call(owner)
            val newEmbedded = kind.embeddedMeta.copy(embedded, predicate, block)
            if (newEmbedded == null) null else copyParam to newEmbedded
        }
        else -> {
            if (predicate(this)) {
                val newValue = block(this) { call(owner) }
                copyParam to newValue
            } else {
                null
            }
        }
    }

    fun getValues(owner: T, predicate: (PropMeta<*, *>) -> Boolean): List<Value> = when (kind) {
        is PropKind.Embedded -> {
            val embedded = call(owner)
            kind.embeddedMeta.getValues(embedded, predicate)
        }
        else -> {
            if (predicate(this)) {
                val obj = prop.call(owner)
                listOf(Value(obj, type))
            } else {
                emptyList()
            }
        }
    }

    private fun call(owner: T): R = prop.call(owner)

    fun getLeafPropMetaList(): List<PropMeta<*, *>> = when (kind) {
        is PropKind.Embedded -> kind.embeddedMeta.getLeafPropMetaList()
        else -> listOf(this)
    }

    fun next(key: String, callNextValue: (String) -> Long): R = when (kind) {
        is PropKind.Id.Sequence -> kind.next(key, callNextValue)
        else -> error("illegal invocation: $kind")
    }

    @Suppress("UNCHECKED_CAST")
    fun inc(value: Any): R = when (kind) {
        is PropKind.Version<R> -> kind.inc(value as R)
        else -> error("illegal invocation: $kind")
    }

    fun now(): R = when (kind) {
        is PropKind.CreatedAt<R> -> kind.now()
        is PropKind.UpdatedAt<R> -> kind.now()
        else -> error("illegal invocation: $kind")
    }
}
