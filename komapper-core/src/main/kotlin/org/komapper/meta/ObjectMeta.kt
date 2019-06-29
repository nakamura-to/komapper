package org.komapper.meta

import org.komapper.value.Value
import kotlin.reflect.KProperty1

data class ObjectMeta(val props: Collection<KProperty1<*, *>>) {
    fun toMap(any: Any): Map<String, Value> {
        return props.associate { it.name to Value(it.call(any), it.returnType) }
    }
}

