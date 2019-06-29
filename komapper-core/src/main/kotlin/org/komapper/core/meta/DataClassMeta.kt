package org.komapper.core.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class DataClassMeta<T : Any>(clazz: KClass<T>, propMetaFactory: PropMetaFactory, hierarchy: List<KClass<*>>) {
    val cons = clazz.primaryConstructor ?: error("The clazz has no primary constructor.")
    val copy = clazz.memberFunctions.find {
        it.name == "copy" && it.returnType.jvmErasure == clazz && it.parameters.size == cons.parameters.size + 1
    }?.let {
        @Suppress("UNCHECKED_CAST")
        it as KFunction<T>
    } ?: error("The clazz does'n have a copy function.")
    val propMetaList = cons.parameters
        .zip(copy.parameters.subList(1, copy.parameters.size))
        .map { (consParam, copyParam) ->
            check(consParam.type == copyParam.type) { "${consParam.type} is not equal to ${copyParam.type}" }
            val prop = clazz.memberProperties.find { it.name == consParam.name!! }
                ?: error("The property \"${consParam.name}\" is not found.")
            check(consParam.type == prop.returnType) { "${consParam.type} is not equal to ${prop.returnType}" }
            propMetaFactory.create(consParam, copyParam, prop, hierarchy)
        }
}
