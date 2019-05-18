package koma.meta

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

data class ObjectMeta(val kClass: KClass<*>) {
    val props: List<KProperty1<*, *>> = kClass.memberProperties.filter { it.getter.isAccessible }
    // TODO
    // fun toExprCtx(obj: Any) : Map<String, Pair<Any, KClass>> {}
}