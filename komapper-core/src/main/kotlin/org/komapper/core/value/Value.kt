package org.komapper.core.value

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

data class Value(val obj: Any?, val type: KClass<*>) {
    constructor(obj: Any?, type: KType) : this(obj, type.jvmErasure)
    constructor(obj: Any) : this(obj, obj::class)
}
