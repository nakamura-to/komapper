package org.komapper.meta

import org.komapper.Value
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

val emptyObject = object {}

fun toMap(obj: Any): Map<String, Value> {
    val meta = getObjectMeta(obj::class)
    return meta.toMap(obj)
}

private data class ObjectMeta(val props: Collection<KProperty1<*, *>>) {
    fun toMap(obj: Any): Map<String, Value> {
        return props
            .associate { it.name to (it.call(obj) to it.returnType.jvmErasure) }
    }
}

private val cache = ConcurrentHashMap<KClass<*>, ObjectMeta>()

private fun <T : Any> getObjectMeta(clazz: KClass<T>): ObjectMeta {
    return cache.computeIfAbsent(clazz) { c ->
        val props: Collection<KProperty1<*, *>> = c.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
        ObjectMeta(props)
    }
}
