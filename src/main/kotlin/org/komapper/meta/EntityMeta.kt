package org.komapper.meta

import org.komapper.Dialect
import org.komapper.Value
import org.komapper.sql.Sql
import org.komapper.sql.SqlBuffer
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class EntityMeta<T>(
    val dialect: Dialect,
    val constructor: KFunction<T>,
    val copyFun: KFunction<T>,
    val tableName: String,
    val propMetaList: List<PropMeta<T>>
) {
    val columnNamePropMetaMap = propMetaList.associateBy { it.columnName }
    val propNamePropMetaMap = propMetaList.associateBy { it.prop.name }
    val idPropMetaList = propMetaList.filter { it.kind is PropKind.Id }
    val versionPropMeta = propMetaList.find { it.kind == PropKind.Version }
    val createdAtPropMeta = propMetaList.filter { it.kind is PropKind.CreatedAt }
    val updatedAtPropMeta = propMetaList.filter { it.kind is PropKind.UpdatedAt }

    fun new(args: Map<KParameter, Any?>): T {
        return constructor.callBy(args)
    }

    fun copy(args: Map<KParameter, Any?>): T {
        return copyFun.callBy(args)
    }

    fun assignId(entity: T, key: String, callNextValue: (String) -> Long): T {
        val idArgs = idPropMetaList
            .map { it to it.kind }
            .filter { (_, kind) -> kind is PropKind.Id.Sequence }
            .map { (meta, kind) ->
                meta.copyFunParam to when (kind) {
                    is PropKind.Id.Sequence -> kind.next(key, callNextValue)
                    else -> throw AssertionError("unreachable: $kind")
                }
            }.map(::convertType)
        if (idArgs.isEmpty()) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copyFun.parameters[0] to entity
        val args = mutableMapOf(receiverArg) + idArgs
        return copy(args)
    }

    fun assignTimestamp(entity: T): T {
        if (createdAtPropMeta.isEmpty()) {
            return entity
        }
        val createdAtArgs = createdAtPropMeta.map {
            it.copyFunParam to when (it.type) {
                LocalDate::class -> LocalDate.now()
                LocalDateTime::class -> LocalDateTime.now()
                LocalTime::class -> LocalTime.now()
                else -> TODO()
            }
        }
        val receiverArg: Pair<KParameter, *> = copyFun.parameters[0] to entity
        val args = mutableMapOf(receiverArg) + createdAtArgs
        return copy(args)
    }

    private fun convertType(pair: Pair<KParameter, Long>): Pair<KParameter, Any> {
        val (param, long) = pair
        return param to when (param.type.jvmErasure) {
            Byte::class -> long.toByte()
            Short::class -> long.toShort()
            Int::class -> long.toInt()
            Long::class -> long
            BigInteger::class -> BigInteger.valueOf(long)
            BigDecimal::class -> BigDecimal.valueOf(long)
            String::class -> long.toString()
            else -> TODO()
        }
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

    fun updateTimestamp(entity: T): T {
        if (updatedAtPropMeta.isEmpty()) {
            return entity
        }
        val createdAtArgs = updatedAtPropMeta.map {
            it.copyFunParam to when (it.type) {
                LocalDate::class -> LocalDate.now()
                LocalDateTime::class -> LocalDateTime.now()
                LocalTime::class -> LocalTime.now()
                else -> TODO()
            }
        }
        val receiverArg: Pair<KParameter, *> = copyFun.parameters[0] to entity
        val args = mutableMapOf(receiverArg) + createdAtArgs
        return copy(args)
    }

    fun buildSelectSql(criteria: org.komapper.criteria.Criteria<T>): Sql {
        val buf = SqlBuffer(dialect::formatValue)
        buf.append("select ")
        propMetaList.forEach { meta ->
            buf.append("${meta.columnName}, ")
        }
        buf.cutBack(2)
        buf.append(" from $tableName")
        with(criteria) {
            if (whereScope.criterionList.isNotEmpty()) {
                buf.append(" where ")
                visit(buf, whereScope.criterionList)
            }
            if (orderByScope.items.isNotEmpty()) {
                buf.append(" order by ")
                orderByScope.items.forEach { (meta, sort) ->
                    val propMeta = propNamePropMetaMap[meta.name] ?: TODO()
                    buf.append(propMeta.columnName)
                    buf.append(" $sort, ")
                }
                buf.cutBack(2)
            }
            limit?.let { buf.append(" limit $limit") }
            offset?.let { buf.append(" offset $offset") }
        }
        return buf.toSql()

    }

    private fun visit(buf: SqlBuffer, criterionList: List<org.komapper.criteria.Criterion>) {
        criterionList.forEachIndexed { index, criterion ->
            when (criterion) {
                is org.komapper.criteria.Criterion.Eq -> op(buf, "=", criterion.prop, criterion.value)
                is org.komapper.criteria.Criterion.Ne -> op(buf, "<>", criterion.prop, criterion.value)
                is org.komapper.criteria.Criterion.Gt -> op(buf, ">", criterion.prop, criterion.value)
                is org.komapper.criteria.Criterion.Ge -> op(buf, ">=", criterion.prop, criterion.value)
                is org.komapper.criteria.Criterion.Lt -> op(buf, "<", criterion.prop, criterion.value)
                is org.komapper.criteria.Criterion.Le -> op(buf, "<=", criterion.prop, criterion.value)
                is org.komapper.criteria.Criterion.And -> logicalOp(buf, "and", index, criterion.criterionList)
                is org.komapper.criteria.Criterion.Or -> logicalOp(buf, "or", index, criterion.criterionList)
                is org.komapper.criteria.Criterion.In -> inOp(buf, criterion.prop, criterion.values)
            }
            buf.append(" and ")
        }
        buf.cutBack(5)
    }

    private fun op(buf: SqlBuffer, op: String, prop: KProperty1<*, *>, value: Any?) {
        val meta = propNamePropMetaMap[prop.name] ?: TODO()
        buf.append(meta.columnName)
        buf.append(" $op ")
        buf.bind(value to prop.returnType.jvmErasure)
    }

    private fun logicalOp(
        buf: SqlBuffer,
        op: String,
        index: Int,
        criterionList: List<org.komapper.criteria.Criterion>
    ) {
        if (index > 0) {
            buf.cutBack(5)
            buf.append(" $op ")
        }
        buf.append("(")
        visit(buf, criterionList)
        buf.append(")")
    }

    private fun inOp(buf: SqlBuffer, prop: KProperty1<*, *>, values: Iterable<*>) {
        val meta = propNamePropMetaMap[prop.name] ?: TODO()
        buf.append(meta.columnName)
        buf.append(" in (")
        val type = prop.returnType.jvmErasure
        var counter = 0
        for (v in values) {
            if (++counter > 1) buf.append(", ")
            buf.bind(v to type)
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }


    fun buildFindByIdSql(id: Any, version: Any?): Sql {
        val buf = SqlBuffer(dialect::formatValue)
        buf.append("select ")
        propMetaList.forEach { meta ->
            buf.append("${meta.columnName}, ")
        }
        buf.cutBack(2)
        buf.append(" from $tableName where ")
        when (id) {
            is Collection<*> -> {
                require(id.size == idPropMetaList.size)
                id.zip(idPropMetaList).forEach { (obj, meta) ->
                    buf.append("${meta.columnName} = ")
                    buf.bind(obj to meta.type)
                    buf.append(" and ")
                }
            }
            else -> {
                require(idPropMetaList.size == 1)
                buf.append("${idPropMetaList[0].columnName} = ")
                buf.bind(id to idPropMetaList[0].type)
                buf.append(" and ")
            }
        }
        buf.cutBack(5)
        if (version != null) {
            requireNotNull(versionPropMeta)
            buf.append(" and ")
            buf.append("${versionPropMeta.columnName} = ")
            buf.bind(version to versionPropMeta.type)
        }
        return buf.toSql()
    }

    fun buildInsertSql(newEntity: T): Sql {
        val buf = SqlBuffer(dialect::formatValue)
        buf.append("insert into $tableName")
        buf.append(" (")
        propMetaList.forEach { meta ->
            buf.append("${meta.columnName}, ")
        }
        buf.cutBack(2)
        buf.append(") values(")
        propMetaList.forEach { meta ->
            buf.bind(meta.getValue(newEntity))
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toSql()
    }

    fun buildDeleteSql(entity: T): Sql {
        val buf = SqlBuffer(dialect::formatValue)
        buf.append("delete from $tableName")
        if (idPropMetaList.isNotEmpty()) {
            buf.append(" where ")
            idPropMetaList.forEach { meta ->
                buf.append("${meta.columnName} = ")
                buf.bind(meta.getValue(entity))
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        if (versionPropMeta != null) {
            if (idPropMetaList.isEmpty()) {
                buf.append(" where ")
            } else {
                buf.append(" and ")
            }
            buf.append("${versionPropMeta.columnName} = ")
            buf.bind(versionPropMeta.getValue(entity))
        }
        return buf.toSql()
    }

    fun buildUpdateSql(entity: T, newEntity: T): Sql {
        val buf = SqlBuffer(dialect::formatValue)
        buf.append("update $tableName")
        buf.append(" set ")
        propMetaList.filter { it.kind !is PropKind.Id }.forEach { meta ->
            buf.append("${meta.columnName} = ")
            buf.bind(meta.getValue(newEntity))
            buf.append(", ")
        }
        buf.cutBack(2)
        if (idPropMetaList.isNotEmpty()) {
            buf.append(" where ")
            idPropMetaList.forEach { meta ->
                buf.append("${meta.columnName} = ")
                buf.bind(meta.getValue(newEntity))
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        if (versionPropMeta != null) {
            if (idPropMetaList.isEmpty()) {
                buf.append(" where ")
            } else {
                buf.append(" and ")
            }
            buf.append("${versionPropMeta.columnName} = ")
            buf.bind(versionPropMeta.getValue(entity))
        }
        return buf.toSql()
    }

}

private val cache = ConcurrentHashMap<KClass<*>, EntityMeta<*>>()

fun <T : Any> getEntityMeta(
    clazz: KClass<T>,
    dialect: Dialect,
    namingStrategy: org.komapper.NamingStrategy
): EntityMeta<T> {
    @Suppress("UNCHECKED_CAST")
    return cache.computeIfAbsent(clazz) { makeEntityMeta(it, dialect, namingStrategy) } as EntityMeta<T>
}

private fun <T : Any> makeEntityMeta(
    clazz: KClass<T>,
    dialect: Dialect,
    namingStrategy: org.komapper.NamingStrategy
): EntityMeta<T> {
    require(clazz.isData) { "The clazz must be a data class." }
    require(!clazz.isAbstract) { "The clazz must not be abstract." }
    val constructor = clazz.primaryConstructor ?: throw AssertionError()
    val copyFun = clazz.memberFunctions.find {
        it.name == "copy" && it.returnType.jvmErasure == clazz
    }?.let {
        @Suppress("UNCHECKED_CAST")
        it as KFunction<T>
    } ?: TODO()
    val table = clazz.findAnnotation<org.komapper.Table>()
    val tableName = table?.name ?: namingStrategy.fromKotlinToDb(clazz.simpleName!!)
    val propMetaList = constructor.parameters
        .zip(copyFun.parameters.subList(1, copyFun.parameters.size))
        .map { (consParam, copyFunParam) ->
            val prop = clazz.memberProperties.find { prop ->
                consParam.name!! == prop.name
            } ?: TODO()
            makePropMeta(consParam, copyFunParam, prop, namingStrategy)
        }
    return EntityMeta(dialect, constructor, copyFun, tableName, propMetaList)
}
