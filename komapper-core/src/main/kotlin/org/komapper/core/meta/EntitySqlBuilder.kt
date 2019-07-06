package org.komapper.core.meta

import org.komapper.core.DeleteOption
import org.komapper.core.InsertOption
import org.komapper.core.UpdateOption
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface EntitySqlBuilder {
    fun <T> buildFindById(entityMeta: EntityMeta<T>, id: Any, versionValue: Any?): Sql
    fun <T> buildInsert(entityMeta: EntityMeta<T>, newEntity: T, option: InsertOption): Sql
    fun <T> buildDelete(entityMeta: EntityMeta<T>, entity: T, option: DeleteOption): Sql
    fun <T> buildUpdate(entityMeta: EntityMeta<T>, entity: T, newEntity: T, option: UpdateOption): Sql
    fun <T> buildMerge(
        entityMeta: EntityMeta<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Sql

    fun <T> buildUpsert(
        entityMeta: EntityMeta<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Sql
}

open class DefaultEntitySqlBuilder(
    @Suppress("MemberVisibilityCanBePrivate")
    protected val formatter: (Any?, KClass<*>) -> String

) : EntitySqlBuilder {

    override fun <T> buildFindById(entityMeta: EntityMeta<T>, id: Any, versionValue: Any?): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("select ")
            leafPropMetaList.forEach { buf.append("${it.columnName}, ") }
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

    override fun <T> buildInsert(entityMeta: EntityMeta<T>, newEntity: T, option: InsertOption): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("insert into $tableName (")
            val propMetaList = leafPropMetaList
                .filter { option.include.isEmpty() || it.prop in option.include }
                .filter { option.exclude.isEmpty() || it.prop !in option.exclude }
            propMetaList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(") values (")
            propMetaList.forEach {
                val value = it.getValue(newEntity as Any)
                buf.bind(value).append(", ")
            }
            buf.cutBack(2).append(")")
            return buf.toSql()
        }
    }

    override fun <T> buildDelete(entityMeta: EntityMeta<T>, entity: T, option: DeleteOption): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer()
            buf.append("delete from $tableName")
            buildWhereClauseForIdAndVersion(entityMeta, entity, option.ignoreVersion, buf)
            return buf.toSql()
        }
    }

    override fun <T> buildUpdate(entityMeta: EntityMeta<T>, entity: T, newEntity: T, option: UpdateOption): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("update $tableName set ")
            nonIdList
                .filter { option.include.isEmpty() || it.prop in option.include }
                .filter { option.exclude.isEmpty() || it.prop !in option.exclude }
                .forEach {
                    val value = it.getValue(newEntity as Any)
                    buf.append("${it.columnName} = ").bind(value).append(", ")
                }
            buf.cutBack(2)
            buildWhereClauseForIdAndVersion(entityMeta, entity, option.ignoreVersion, buf)
            return buf.toSql()
        }
    }

    override fun <T> buildMerge(
        entityMeta: EntityMeta<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("merge into $tableName using dual on ")
            if (keys.isNotEmpty()) {
                val keyPropMetaList = keys.map { prop ->
                    propMap[prop] ?: error(
                        "The property \"${prop.name}\" is not found " +
                                "in the class \"${entityMeta.type.qualifiedName}\""
                    )
                }
                keyPropMetaList.forEach {
                    val value = it.getValue(newEntity)
                    buf.append("${it.columnName} = ").bind(value).append(" and ")
                }
            } else {
                idList.forEach {
                    val value = it.getValue(newEntity)
                    buf.append("${it.columnName} = ").bind(value).append(" and ")
                }
            }
            buf.cutBack(5)
            buf.append(" when not matched then insert (")
            val propMetaList = leafPropMetaList
                .filter { insertOption.include.isEmpty() || it.prop in insertOption.include }
                .filter { insertOption.exclude.isEmpty() || it.prop !in insertOption.exclude }
            propMetaList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(") values (")
            propMetaList.forEach {
                val value = it.getValue(newEntity)
                buf.bind(value).append(", ")
            }
            buf.cutBack(2).append(")")
            buf.append(" when matched")
            if (!updateOption.ignoreVersion && version != null) {
                buf.append(" and ${version.columnName} = ")
                    .bind(version.getValue(entity))
            }
            buf.append(" then update set ")
            nonIdList
                .filter { updateOption.include.isEmpty() || it.prop in updateOption.include }
                .filter { updateOption.exclude.isEmpty() || it.prop !in updateOption.exclude }
                .forEach {
                    val value = it.getValue(newEntity)
                    buf.append("${it.columnName} = ").bind(value).append(", ")
                }
            buf.cutBack(2)
            return buf.toSql()
        }
    }

    override fun <T> buildUpsert(
        entityMeta: EntityMeta<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption

    ): Sql {
        with(entityMeta) {
            val buf = newSqlBuffer().append("insert into $tableName as t_ (")
            val propMetaList = leafPropMetaList
                .filter { insertOption.include.isEmpty() || it.prop in insertOption.include }
                .filter { insertOption.exclude.isEmpty() || it.prop !in insertOption.exclude }
            propMetaList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(") values(")
            propMetaList.forEach {
                val value = it.getValue(newEntity)
                buf.bind(value).append(", ")
            }
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
                idList.forEach { propMeta -> buf.append("${propMeta.columnName}, ") }
            }
            buf.cutBack(2).append(")")
            buf.append(" do update set ")
            nonIdList
                .filter { updateOption.include.isEmpty() || it.prop in updateOption.include }
                .filter { updateOption.exclude.isEmpty() || it.prop !in updateOption.exclude }
                .forEach {
                    val value = it.getValue(newEntity)
                    buf.append("${it.columnName} = ").bind(value).append(", ")
                }
            buf.cutBack(2)
            if (!updateOption.ignoreVersion && version != null) {
                buf.append(" where ${version.columnName} = ")
                    .bind(version.getValue(entity))
            }
            return buf.toSql()
        }
    }

    protected open fun <T> buildWhereClauseForIdAndVersion(
        entityMeta: EntityMeta<T>,
        entity: T,
        ignoreVersion: Boolean,
        buf: SqlBuffer
    ) {
        with(entityMeta) {
            if (idList.isNotEmpty()) {
                buf.append(" where ")
                idList.forEach {
                    val value = it.getValue(entity)
                    buf.append("${it.columnName} = ").bind(value).append(" and ")
                }
                buf.cutBack(5)
            }
            if (!ignoreVersion && version != null) {
                buf.append(if (idList.isEmpty()) " where " else " and ")
                    .append("${version.columnName} = ")
                    .bind(version.getValue(entity))
            }
        }
    }

    protected open fun newSqlBuffer(): SqlBuffer {
        return SqlBuffer(formatter)
    }

    protected fun <T> PropMeta<*, *>.getValue(entity: T): Value {
        val obj = this.deepGetter(entity as Any)
        return Value(obj, this.type)
    }

}
