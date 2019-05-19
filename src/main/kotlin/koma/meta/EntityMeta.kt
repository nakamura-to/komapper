package koma.meta

import koma.Table
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class EntityMeta(
    val constructor: KFunction<*>,
    val tableName: String,
    val paramMap: Map<String, KParameter>,
    val propList: List<PropMeta>,
    val idPropList: List<PropMeta>,
    val versionProp: PropMeta?
) {
    fun new(args: Map<KParameter, Any?>): Any? {
        return constructor.callBy(args)
    }

    fun copy(args: Map<KParameter, Any?>): Any? {
        TODO()
    }
}

fun makeEntityMeta(kClass: KClass<*>): EntityMeta {
    if (!kClass.isData) throw AssertionError()
    if (kClass.isAbstract) throw AssertionError()
    val constructor = kClass.primaryConstructor ?: throw AssertionError()
    val table = kClass.findAnnotation<Table>()
    val tableName = table?.name ?: kClass.simpleName!!
    val paramMap = constructor.parameters.associateBy { it.name!! }
    val propList = paramMap.values.map { param ->
        val prop = kClass.memberProperties.find { prop ->
            param.name!! == prop.name
        } ?: TODO()
        makePropMeta(param, prop)
    }
    val idPropList = propList.filter { it.kind == PropKind.Id }
    val versionProp = propList.find { it.kind == PropKind.Version }
    return EntityMeta(constructor, tableName, paramMap, propList, idPropList, versionProp)
}
