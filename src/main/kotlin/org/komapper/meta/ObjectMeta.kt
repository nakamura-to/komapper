package org.komapper.meta

import org.komapper.Value
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

data class ObjectMeta(val props: Collection<KProperty1<*, *>>) {
    fun toMap(any: Any): Map<String, Value> {
        return props
            .associate { it.name to (it.call(any) to it.returnType.jvmErasure) }
    }
}

