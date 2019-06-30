package org.komapper.core.meta

import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value
import kotlin.reflect.KProperty1

interface EntitySqlBuilder {
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
