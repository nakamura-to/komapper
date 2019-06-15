package org.komapper.query

import org.komapper.criteria.Criteria
import org.komapper.jdbc.Dialect
import org.komapper.meta.EntityMeta
import org.komapper.meta.PropKind
import org.komapper.sql.Sql
import org.komapper.sql.SqlBuffer

interface QueryBuilder {
    fun <T> buildSelect(entityMeta: EntityMeta<T>, criteria: Criteria<T>): Sql
    fun <T> buildFindById(entityMeta: EntityMeta<T>, id: Any, versionValue: Any?): Sql
    fun <T> buildInsert(entityMeta: EntityMeta<T>, newEntity: T): Sql
    fun <T> buildDelete(entityMeta: EntityMeta<T>, entity: T): Sql
    fun <T> buildUpdate(entityMeta: EntityMeta<T>, entity: T, newEntity: T): Sql
}

open class DefaultQueryBuilder(
    @Suppress("MemberVisibilityCanBePrivate")
    protected val dialect: Dialect
) : QueryBuilder {

    override fun <T> buildSelect(entityMeta: EntityMeta<T>, criteria: Criteria<T>): Sql {
        val buf = newSqlBuffer()
        val traversal = CriteriaTraversal(entityMeta, buf)
        with(entityMeta) {
            buf.append("select ")
            propMetaList.forEach { propMeta ->
                buf.append("${propMeta.columnName}, ")
            }
            buf.cutBack(2).append(" from $tableName")
            traversal.run(criteria)
            return buf.toSql()
        }
    }

    override fun <T> buildFindById(entityMeta: EntityMeta<T>, id: Any, versionValue: Any?): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("select ")
            propMetaList.forEach { meta ->
                buf.append("${meta.columnName}, ")
            }
            buf.cutBack(2).append(" from $tableName where ")
            when (id) {
                is Collection<*> -> {
                    require(id.size == idList.size) { "The number of id must be {$idList.size}." }
                    id.zip(idList).forEach { (obj, propMeta) ->
                        buf.append("${propMeta.columnName} = ").bind(obj to propMeta.type).append(" and ")
                    }
                }
                else -> {
                    require(idList.size == 1) { "The number of id must be ${idList.size}." }
                    buf.append("${idList[0].columnName} = ").bind(id to idList[0].type).append(" and ")
                }
            }
            buf.cutBack(5)
            if (versionValue != null && version != null) {
                buf.append(" and ").append("${version.columnName} = ").bind(versionValue to version.type)
            }
            return buf.toSql()
        }
    }

    override fun <T> buildInsert(entityMeta: EntityMeta<T>, newEntity: T): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("insert into $tableName (")
            propMetaList.forEach { meta ->
                buf.append("${meta.columnName}, ")
            }
            buf.cutBack(2).append(") values(")
            propMetaList.forEach { propMeta ->
                buf.bind(propMeta.getValue(newEntity)).append(", ")
            }
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
            propMetaList.filter { it.kind !is PropKind.Id }.forEach { propMeta ->
                buf.append("${propMeta.columnName} = ").bind(propMeta.getValue(newEntity)).append(", ")
            }
            buf.cutBack(2)
            buildWhereClauseForIdAndVersion(entityMeta, entity, buf)
            return buf.toSql()
        }
    }

    protected open fun <T> buildWhereClauseForIdAndVersion(entityMeta: EntityMeta<T>, entity: T, buf: SqlBuffer) {
        with(entityMeta) {
            if (idList.isNotEmpty()) {
                buf.append(" where ")
                idList.forEach { propMeta ->
                    buf.append("${propMeta.columnName} = ").bind(propMeta.getValue(entity)).append(" and ")
                }
                buf.cutBack(5)
            }
            if (version != null) {
                buf.append(" ")
                    .append(if (idList.isEmpty()) "where" else "and")
                    .append(" ${version.columnName} = ")
                    .bind(version.getValue(entity))
            }
        }
    }

    protected open fun newSqlBuffer(): SqlBuffer {
        return SqlBuffer(dialect::formatValue)
    }

}
