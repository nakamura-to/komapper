package koma.meta

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class EntityMeta(val constructor: KFunction<*>) {
    val propMetaMap: Map<String, PropMeta> =
        constructor.parameters.associateBy({ it.name!!.toString() }, { makePropMeta(it) })

    fun new(args: Map<KParameter, Any?>): Any? {
        return constructor.callBy(args)
    }
}

fun makeEntityMeta(kClass: KClass<*>): EntityMeta {
    if (!kClass.isData) throw AssertionError()
    if (kClass.isAbstract) throw AssertionError()
    val constructor = kClass.primaryConstructor ?: throw AssertionError()
    return EntityMeta(constructor)
}
