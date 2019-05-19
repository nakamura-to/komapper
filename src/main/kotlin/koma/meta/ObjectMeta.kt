package koma.meta

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.jvmErasure

data class ObjectMeta(val kClass: KClass<*>) {
    val props: Collection<KProperty1<*, *>> = kClass.memberProperties.filter { it.javaGetter != null }
    fun toMap(obj: Any): Map<String, Pair<*, KClass<*>>> {
        return props
            .filter { it.javaGetter!!.canAccess(obj) }
            .associate { it.name to (it.call(obj) to it.returnType.jvmErasure) }
    }
}