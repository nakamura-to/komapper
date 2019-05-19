package koma.meta

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

data class ObjectMeta(val kClass: KClass<*>) {
    val props: Collection<KProperty1<*, *>> = kClass.memberProperties.filter { it.visibility == KVisibility.PUBLIC }
    fun toMap(obj: Any): Map<String, Pair<*, KClass<*>>> {
        return props
            .associate { it.name to (it.call(obj) to it.returnType.jvmErasure) }
    }
}