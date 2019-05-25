package koma.meta

import koma.Table
import koma.Value
import koma.sql.Buffer
import koma.sql.Sql
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class EntityMeta<T>(
    val constructor: KFunction<T>,
    val copyFun: KFunction<T>,
    val tableName: String,
    val consParamMap: Map<String, KParameter>,
    val propMetaList: List<PropMeta<T>>,
    val idPropMetaList: List<PropMeta<T>>,
    val versionPropMeta: PropMeta<T>?
) {
    fun new(args: Map<KParameter, Any?>): T {
        return constructor.callBy(args)
    }

    fun copy(args: Map<KParameter, Any?>): T {
        return copyFun.callBy(args)
    }

    // TODO
    fun assignId(entity: T): T {
        return entity
    }

    fun incrementVersion(entity: T): T {
        if (versionPropMeta == null) {
            return entity
        }
        val version = versionPropMeta.getValue(entity)
        val (newVersion) = increment(version)
        val receiverArg = copyFun.parameters[0] to entity
        val versionArg = versionPropMeta.copyFunParam to newVersion
        return copy(mapOf(receiverArg, versionArg))
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun increment(value: Value): Value {
        val (first) = value
        val v = when (first) {
            is Byte -> first.inc()
            is Short -> first.inc()
            is Int -> first.inc()
            is Long -> first.inc()
            is BigDecimal -> first.inc()
            is BigInteger -> first.inc()
            else -> TODO()
        }
        return value.copy(v)
    }


    fun buildInsertSql(newEntity: T): Sql {
        val buf = Buffer()
        buf.append("insert into $tableName")
        buf.append(" (")
        val propList = propMetaList
        propList.forEach { prop ->
            buf.append("${prop.columnName}, ")
        }
        buf.cutBack(2)
        buf.append(") values(")
        propList.forEach { prop ->
            buf.bind(prop.getValue(newEntity))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return Sql(buf.sql.toString(), buf.values, buf.log.toString())
    }

    fun buildDeleteSql(entity: T): Sql {
        val buf = Buffer()
        buf.append("delete from $tableName")
        val idPropList = idPropMetaList
        if (idPropList.isNotEmpty()) {
            buf.append(" where ")
            idPropList.forEach {
                buf.append("${it.columnName} = ")
                buf.bind(it.getValue(entity))
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        val versionProp = versionPropMeta
        if (versionProp != null) {
            if (idPropList.isEmpty()) {
                buf.append(" where ")
            } else {
                buf.append(" and ")
            }
            buf.append("${versionProp.columnName} = ")
            buf.bind(versionProp.getValue(entity))
        }
        return Sql(buf.sql.toString(), buf.values, buf.log.toString())
    }

    fun buildUpdateSql(entity: T, newEntity: T): Sql {
        val buf = Buffer()
        buf.append("update $tableName")
        buf.append(" set ")
        val propList = propMetaList
        propList.filter { it.kind != PropKind.Id }.forEach { prop ->
            buf.append("${prop.columnName} = ")
            buf.bind(prop.getValue(newEntity))
            buf.append(", ")
        }
        buf.cutBack(2)
        val idPropList = idPropMetaList
        if (idPropList.isNotEmpty()) {
            buf.append(" where ")
            idPropList.forEach {
                buf.append("${it.columnName} = ")
                buf.bind(it.getValue(newEntity))
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        val versionProp = versionPropMeta
        if (versionProp != null) {
            if (idPropList.isEmpty()) {
                buf.append(" where ")
            } else {
                buf.append(" and ")
            }
            buf.append("${versionProp.columnName} = ")
            buf.bind(versionProp.getValue(entity))
        }
        return Sql(buf.sql.toString(), buf.values, buf.log.toString())
    }

}

fun <T : Any> makeEntityMeta(clazz: KClass<T>): EntityMeta<T> {
    require(clazz.isData) { "The clazz must be a data class." }
    require(!clazz.isAbstract) { "The clazz must not be abstract." }
    val constructor = clazz.primaryConstructor ?: throw AssertionError()
    val copyFun = clazz.memberFunctions.find {
        it.name == "copy" && it.returnType.jvmErasure == clazz
    }?.let {
        it as KFunction<T>
    } ?: TODO()
    val table = clazz.findAnnotation<Table>()
    val tableName = table?.name ?: clazz.simpleName!!
    val consParamMap = constructor.parameters.associateBy { it.name!! }
    val propMetaList = constructor.parameters
        .zip(copyFun.parameters.subList(1, copyFun.parameters.size))
        .map { (consParam, copyFunParam) ->
            val prop = clazz.memberProperties.find { prop ->
                consParam.name!! == prop.name
            } ?: TODO()
            makePropMeta(consParam, copyFunParam, prop)
        }
    val idPropMetaList = propMetaList.filter { it.kind == PropKind.Id }
    val versionPropMeta = propMetaList.find { it.kind == PropKind.Version }
    return EntityMeta(constructor, copyFun, tableName, consParamMap, propMetaList, idPropMetaList, versionPropMeta)
}
