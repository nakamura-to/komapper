package org.komapper.core.meta

import kotlin.reflect.KProperty1
import org.komapper.core.value.Value

data class ObjectMeta(val props: Collection<KProperty1<*, *>>) {
    fun toMap(any: Any): Map<String, Value> {
        return props.associate { it.name to Value(it.call(any), it.returnType) }
    }
}
