package koma.meta

import koma.Table
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class EntityMeta(
    val constructor: KFunction<*>,
    val copy: KFunction<*>,
    val tableName: String,
    val consParamMap: Map<String, KParameter>,
    val propMetaList: List<PropMeta>,
    val idPropMetaList: List<PropMeta>,
    val versionPropMeta: PropMeta?
) {
    fun new(args: Map<KParameter, Any?>): Any? {
        return constructor.callBy(args)
    }

    fun copy(args: Map<KParameter, Any?>): Any? {
        return copy.callBy(args)
    }
}

fun makeEntityMeta(kClass: KClass<*>): EntityMeta {
    if (!kClass.isData) throw AssertionError()
    if (kClass.isAbstract) throw AssertionError()
    val constructor = kClass.primaryConstructor ?: throw AssertionError()
    val copyFun = kClass.memberFunctions.find { it.name == "copy" && it.returnType.jvmErasure == kClass } ?: TODO()
    val table = kClass.findAnnotation<Table>()
    val tableName = table?.name ?: kClass.simpleName!!
    val consParamMap = constructor.parameters.associateBy { it.name!! }
    val propMetaList = constructor.parameters
        .zip(copyFun.parameters.subList(1, copyFun.parameters.size))
        .map { (consParam, copyFunParam) ->
            val prop = kClass.memberProperties.find { prop ->
                consParam.name!! == prop.name
            } ?: TODO()
            makePropMeta(consParam, copyFunParam, prop)
        }
    val idPropMetaList = propMetaList.filter { it.kind == PropKind.Id }
    val versionPropMeta = propMetaList.find { it.kind == PropKind.Version }
    return EntityMeta(constructor, copyFun, tableName, consParamMap, propMetaList, idPropMetaList, versionPropMeta)
}
