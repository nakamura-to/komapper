package org.komapper.meta

import org.komapper.criteria.Criteria
import org.komapper.criteria.Criterion
import org.komapper.jdbc.Dialect
import org.komapper.sql.Sql
import org.komapper.sql.SqlBuffer
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

class EntityMeta<T>(
    val dialect: Dialect,
    val cons: KFunction<T>,
    val copy: KFunction<T>,
    val tableName: String,
    val propMetaList: List<PropMeta<T>>
) {
    val columnNameMap = propMetaList.associateBy { it.columnName }
    val propNameMap = propMetaList.associateBy { it.prop.name }
    val idList = propMetaList.filter { it.kind is PropKind.Id }
    val version = propMetaList.find { it.kind is PropKind.Version }
    val createdAt = propMetaList.find { it.kind is PropKind.CreatedAt }
    val updatedAt = propMetaList.find { it.kind is PropKind.UpdatedAt }

    fun new(args: Map<KParameter, Any?>): T {
        return cons.callBy(args)
    }

    fun assignId(entity: T, key: String, callNextValue: (String) -> Long): T {
        val idArgs = idList
            .filter { it.kind is PropKind.Id.Sequence }
            .map { it.copyParam to it.next(key, callNextValue) }
            .filter { (_, value) -> value != null }
        if (idArgs.isEmpty()) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val args = mutableMapOf(receiverArg) + idArgs
        return copy(args)
    }

    fun incrementVersion(entity: T): T {
        if (version == null) {
            return entity
        }
        val receiverArg = copy.parameters[0] to entity
        val versionArg = version.copyParam to version.call(entity).let(version::inc)
        return copy(mapOf(receiverArg, versionArg))
    }

    fun assignTimestamp(entity: T): T {
        if (createdAt == null) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val createdAtArg = createdAt.copyParam to createdAt.now()
        return copy(mapOf(receiverArg, createdAtArg))
    }

    fun updateTimestamp(entity: T): T {
        if (updatedAt == null) {
            return entity
        }
        val receiverArg: Pair<KParameter, *> = copy.parameters[0] to entity
        val updatedAtArg = updatedAt.copyParam to updatedAt.now()
        return copy(mapOf(receiverArg, updatedAtArg))
    }

    private fun copy(args: Map<KParameter, Any?>): T {
        return copy.callBy(args)
    }

    fun buildSelectSql(criteria: Criteria<T>): Sql {
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
                    val propMeta = propNameMap[meta.name] ?: TODO()
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

    private fun visit(buf: SqlBuffer, criterionList: List<Criterion>) {
        criterionList.forEachIndexed { index, criterion ->
            when (criterion) {
                is Criterion.Eq -> op(buf, "=", criterion.prop, criterion.value)
                is Criterion.Ne -> op(buf, "<>", criterion.prop, criterion.value)
                is Criterion.Gt -> op(buf, ">", criterion.prop, criterion.value)
                is Criterion.Ge -> op(buf, ">=", criterion.prop, criterion.value)
                is Criterion.Lt -> op(buf, "<", criterion.prop, criterion.value)
                is Criterion.Le -> op(buf, "<=", criterion.prop, criterion.value)
                is Criterion.In -> listOp(buf, "in", criterion.prop, criterion.values)
                is Criterion.NotIn -> listOp(buf, "not in", criterion.prop, criterion.values)
                is Criterion.Like -> op(buf, "like", criterion.prop, criterion.value)
                is Criterion.NotLike -> op(buf, "not like", criterion.prop, criterion.value)
                is Criterion.Between -> {
                    val meta = propNameMap[criterion.prop.name] ?: TODO()
                    buf.append(meta.columnName)
                    buf.append(" between ")
                    buf.bind(criterion.range.first to criterion.prop.returnType.jvmErasure)
                    buf.append(" and ")
                    buf.bind(criterion.range.second to criterion.prop.returnType.jvmErasure)
                }
                is Criterion.Not -> notOp(buf, criterion.criterionList)
                is Criterion.And -> logicalOp(buf, "and", index, criterion.criterionList)
                is Criterion.Or -> logicalOp(buf, "or", index, criterion.criterionList)
            }
            buf.append(" and ")
        }
        buf.cutBack(5)
    }

    private fun op(buf: SqlBuffer, op: String, prop: KProperty1<*, *>, value: Any?) {
        val meta = propNameMap[prop.name] ?: TODO()
        buf.append(meta.columnName)
        buf.append(" $op ")
        buf.bind(value to prop.returnType.jvmErasure)
    }

    private fun notOp(
        buf: SqlBuffer,
        criterionList: List<Criterion>
    ) {
        buf.append("not ")
        buf.append("(")
        visit(buf, criterionList)
        buf.append(")")
    }

    private fun logicalOp(
        buf: SqlBuffer,
        op: String,
        index: Int,
        criterionList: List<Criterion>
    ) {
        if (index > 0) {
            buf.cutBack(5)
            buf.append(" $op ")
        }
        buf.append("(")
        visit(buf, criterionList)
        buf.append(")")
    }

    private fun listOp(buf: SqlBuffer, op: String, prop: KProperty1<*, *>, values: Iterable<*>) {
        val meta = propNameMap[prop.name] ?: TODO()
        buf.append(meta.columnName)
        buf.append(" $op (")
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
                require(id.size == idList.size)
                id.zip(idList).forEach { (obj, meta) ->
                    buf.append("${meta.columnName} = ")
                    buf.bind(obj to meta.type)
                    buf.append(" and ")
                }
            }
            else -> {
                require(idList.size == 1)
                buf.append("${idList[0].columnName} = ")
                buf.bind(id to idList[0].type)
                buf.append(" and ")
            }
        }
        buf.cutBack(5)
        if (version != null) {
            requireNotNull(this.version)
            buf.append(" and ")
            buf.append("${this.version.columnName} = ")
            buf.bind(version to this.version.type)
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
        if (idList.isNotEmpty()) {
            buf.append(" where ")
            idList.forEach { meta ->
                buf.append("${meta.columnName} = ")
                buf.bind(meta.getValue(entity))
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        if (version != null) {
            if (idList.isEmpty()) {
                buf.append(" where ")
            } else {
                buf.append(" and ")
            }
            buf.append("${version.columnName} = ")
            buf.bind(version.getValue(entity))
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
        if (idList.isNotEmpty()) {
            buf.append(" where ")
            idList.forEach { meta ->
                buf.append("${meta.columnName} = ")
                buf.bind(meta.getValue(newEntity))
                buf.append(" and ")
            }
            buf.cutBack(5)
        }
        if (version != null) {
            if (idList.isEmpty()) {
                buf.append(" where ")
            } else {
                buf.append(" and ")
            }
            buf.append("${version.columnName} = ")
            buf.bind(version.getValue(entity))
        }
        return buf.toSql()
    }

}
