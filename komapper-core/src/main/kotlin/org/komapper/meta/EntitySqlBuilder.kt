package org.komapper.meta

import org.komapper.criteria.Criteria
import org.komapper.criteria.Criterion
import org.komapper.jdbc.Dialect
import org.komapper.sql.Sql
import org.komapper.sql.SqlBuffer
import org.komapper.value.Value
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

interface EntitySqlBuilder {
    fun <T> buildSelect(entityMeta: EntityMeta<T>, criteria: Criteria): Sql
    fun <T> buildFindById(entityMeta: EntityMeta<T>, id: Any, versionValue: Any?): Sql
    fun <T> buildInsert(entityMeta: EntityMeta<T>, newEntity: T): Sql
    fun <T> buildDelete(entityMeta: EntityMeta<T>, entity: T): Sql
    fun <T> buildUpdate(entityMeta: EntityMeta<T>, entity: T, newEntity: T): Sql
    fun <T> buildMerge(entityMeta: EntityMeta<T>, newEntity: T, keys: List<KProperty1<*, *>>): Sql
    fun <T> buildUpsert(entityMeta: EntityMeta<T>, newEntity: T, keys: List<KProperty1<*, *>>): Sql
}

open class DefaultEntitySqlBuilder(
    @Suppress("MemberVisibilityCanBePrivate")
    protected val dialect: Dialect
) : EntitySqlBuilder {

    override fun <T> buildSelect(entityMeta: EntityMeta<T>, criteria: Criteria): Sql {
        val buf = newSqlBuffer()
        val traversal = CriteriaTraversal(entityMeta, buf)
        with(entityMeta) {
            buf.append("select ")
            columnNames.forEach { buf.append("$it, ") }
            buf.cutBack(2).append(" from $tableName")
            traversal.run(criteria)
            return buf.toSql()
        }
    }

    override fun <T> buildFindById(entityMeta: EntityMeta<T>, id: Any, versionValue: Any?): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("select ")
            columnNames.forEach { buf.append("$it, ") }
            buf.cutBack(2).append(" from $tableName where ")
            when (id) {
                is Collection<*> -> {
                    require(id.size == idList.size) { "The number of id must be {$idList.size}." }
                    id.zip(idList).forEach { (obj, propMeta) ->
                        val value = Value(obj, propMeta.type)
                        buf.append("${propMeta.columnName} = ").bind(value).append(" and ")
                    }
                }
                else -> {
                    require(idList.size == 1) { "The number of id must be ${idList.size}." }
                    val value = Value(id, idList[0].type)
                    buf.append("${idList[0].columnName} = ").bind(value).append(" and ")
                }
            }
            buf.cutBack(5)
            if (versionValue != null && version != null) {
                val value = Value(versionValue, version.type)
                buf.append(" and ").append("${version.columnName} = ").bind(value)
            }
            return buf.toSql()
        }
    }

    override fun <T> buildInsert(entityMeta: EntityMeta<T>, newEntity: T): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("insert into $tableName (")
            columnNames.forEach { buf.append("$it, ") }
            buf.cutBack(2).append(") values(")
            getValues(newEntity).forEach { buf.bind(it).append(", ") }
            buf.cutBack(2).append(")")
            return buf.toSql()
        }
    }

    override fun <T> buildDelete(entityMeta: EntityMeta<T>, entity: T): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer()
            buf.append("delete from $tableName")
            buildWhereClauseForIdAndVersion(entityMeta, entity, buf)
            return buf.toSql()
        }
    }

    override fun <T> buildUpdate(entityMeta: EntityMeta<T>, entity: T, newEntity: T): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("update $tableName set ")
            nonIdColumnNames.zip(getNonIdValues(newEntity)).forEach { (columnName, value) ->
                buf.append("$columnName = ").bind(value).append(", ")
            }
            buf.cutBack(2)
            buildWhereClauseForIdAndVersion(entityMeta, entity, buf)
            return buf.toSql()
        }
    }

    override fun <T> buildMerge(entityMeta: EntityMeta<T>, newEntity: T, keys: List<KProperty1<*, *>>): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("merge into $tableName using dual on ")
            if (keys.isNotEmpty()) {
                val leafPropMetaList = keys.map { prop ->
                    propMap[prop] ?: error(
                        "The property \"${prop.name}\" is not found " +
                                "in the class \"${entityMeta.type.qualifiedName}\""
                    )
                }
                val values = entityMeta.getValues(newEntity, { it in leafPropMetaList })
                leafPropMetaList.zip(values).forEach { (propMeta, value) ->
                    buf.append("${propMeta.columnName} = ").bind(value).append(" and ")
                }
            } else {
                idColumnNames.zip(getIdValues(newEntity)).forEach { (columnName, value) ->
                    buf.append("$columnName = ").bind(value).append(" and ")
                }
            }
            buf.cutBack(5)
            buf.append(" when not matched then insert (")
            columnNames.forEach { buf.append("$it, ") }
            buf.cutBack(2).append(") values (")
            getValues(newEntity).forEach { buf.bind(it).append(", ") }
            buf.cutBack(2).append(")")
            buf.append(" when matched then update set ")
            nonIdColumnNames.zip(getNonIdValues(newEntity)).forEach { (columnName, value) ->
                buf.append("$columnName = ").bind(value).append(", ")
            }
            buf.cutBack(2)
            return buf.toSql()
        }
    }

    override fun <T> buildUpsert(entityMeta: EntityMeta<T>, newEntity: T, keys: List<KProperty1<*, *>>): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("insert into $tableName as t_ (")
            columnNames.forEach { buf.append("$it, ") }
            buf.cutBack(2).append(") values(")
            getValues(newEntity).forEach { buf.bind(it).append(", ") }
            buf.cutBack(2).append(") on conflict (")
            if (keys.isNotEmpty()) {
                keys.map { prop ->
                    propMap[prop] ?: error(
                        "The property \"${prop.name}\" is not found " +
                                "in the class \"${entityMeta.type.qualifiedName}\""
                    )
                }.forEach { propMeta ->
                    buf.append("${propMeta.columnName}, ")
                }
            } else {
                idColumnNames.forEach { columnName -> buf.append("$columnName, ") }
            }
            buf.cutBack(2).append(")")
            buf.append(" do update set ")
            nonIdColumnNames.zip(getNonIdValues(newEntity)).forEach { (columnName, value) ->
                buf.append("$columnName = ").bind(value).append(", ")
            }
            buf.cutBack(2)
            return buf.toSql()
        }
    }

    protected open fun <T> buildWhereClauseForIdAndVersion(entityMeta: EntityMeta<T>, entity: T, buf: SqlBuffer) {
        with(entityMeta) {
            if (idList.isNotEmpty()) {
                buf.append(" where ")
                idColumnNames.zip(getIdValues(entity)).forEach { (columnName, value) ->
                    buf.append("$columnName = ").bind(value).append(" and ")
                }
                buf.cutBack(5)
            }
            if (version != null) {
                buf.append(if (idList.isEmpty()) " where " else " and ")
                    .append("${version.columnName} = ")
                    .bind(getVersionValue(entity))
            }
        }
    }

    protected open fun newSqlBuffer(): SqlBuffer {
        return SqlBuffer(dialect::formatValue)
    }

}

class CriteriaTraversal<T>(private val entityMeta: EntityMeta<T>, private val buf: SqlBuffer) {

    fun run(criteria: Criteria) {
        with(criteria) {
            if (whereScope.criterionList.isNotEmpty()) {
                buf.append(" where ")
                visit(whereScope.criterionList)
            }
            if (orderByScope.items.isNotEmpty()) {
                buf.append(" order by ")
                orderByScope.items.forEach { (prop, sort) ->
                    buf.append(resolveColumnName(prop)).append(" $sort, ")
                }
                buf.cutBack(2)
            }
            limit?.let { buf.append(" limit $limit") }
            offset?.let { buf.append(" offset $offset") }
        }
    }

    private fun visit(criterionList: List<Criterion>) {
        criterionList.forEachIndexed { index, criterion ->
            when (criterion) {
                is Criterion.Eq -> visitBinaryOp("=", criterion.prop, criterion.value)
                is Criterion.Ne -> visitBinaryOp("<>", criterion.prop, criterion.value)
                is Criterion.Gt -> visitBinaryOp(">", criterion.prop, criterion.value)
                is Criterion.Ge -> visitBinaryOp(">=", criterion.prop, criterion.value)
                is Criterion.Lt -> visitBinaryOp("<", criterion.prop, criterion.value)
                is Criterion.Le -> visitBinaryOp("<=", criterion.prop, criterion.value)
                is Criterion.Like -> visitBinaryOp("like", criterion.prop, criterion.value)
                is Criterion.NotLike -> visitBinaryOp("not like", criterion.prop, criterion.value)
                is Criterion.In -> visitInOp("in", criterion.prop, criterion.values)
                is Criterion.NotIn -> visitInOp("not in", criterion.prop, criterion.values)
                is Criterion.And -> visitLogicalBinaryOp("and", index, criterion.criterionList)
                is Criterion.Or -> visitLogicalBinaryOp("or", index, criterion.criterionList)
                is Criterion.Not -> visitNotOp(criterion.criterionList)
                is Criterion.Between -> visitBetweenOp(criterion.prop, criterion.range)
            }
            buf.append(" and ")
        }
        buf.cutBack(5)
    }

    private fun visitBinaryOp(op: String, prop: KProperty1<*, *>, obj: Any?) {
        buf.append(resolveColumnName(prop))
        when {
            op == "=" && obj == null -> buf.append(" is null")
            op == "<>" && obj == null -> buf.append(" is not null")
            else -> {
                val value = Value(obj, prop.returnType)
                buf.append(" $op ").bind(value)
            }
        }
    }

    private fun visitInOp(op: String, prop: KProperty1<*, *>, values: Iterable<*>) {
        buf.append(resolveColumnName(prop))
        buf.append(" $op (")
        val type = prop.returnType.jvmErasure
        var counter = 0
        for (v in values) {
            if (++counter > 1) buf.append(", ")
            buf.bind(Value(v, type))
        }
        if (counter == 0) {
            buf.append("null")
        }
        buf.append(")")
    }

    private fun visitLogicalBinaryOp(op: String, index: Int, criterionList: List<Criterion>) {
        if (index > 0) {
            buf.cutBack(5).append(" $op ")
        }
        buf.append("(")
        visit(criterionList)
        buf.append(")")
    }

    private fun visitNotOp(criterionList: List<Criterion>) {
        buf.append("not (")
        visit(criterionList)
        buf.append(")")
    }

    private fun visitBetweenOp(prop: KProperty1<*, *>, range: Pair<*, *>) {
        buf.append(resolveColumnName(prop))
            .append(" between ")
            .bind(Value(range.first, prop.returnType))
            .append(" and ")
            .bind(Value(range.second, prop.returnType))
    }

    private fun resolveColumnName(prop: KProperty1<*, *>): String {
        val propMeta = entityMeta.propMap[prop]
            ?: error(
                "The property \"${prop.name}\" is not found " +
                        "in the class \"${prop.javaField?.declaringClass?.name}\"."
            )
        return propMeta.columnName
    }

}

