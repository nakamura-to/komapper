package org.komapper.core.desc

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.komapper.core.DeleteOption
import org.komapper.core.InsertOption
import org.komapper.core.UpdateOption
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

interface EntitySqlBuilder {
    fun <T : Any> buildFindById(entityDesc: EntityDesc<T>, id: Any, versionValue: Any?): Sql
    fun <T : Any> buildInsert(entityDesc: EntityDesc<T>, newEntity: T, option: InsertOption): Sql
    fun <T : Any> buildDelete(entityDesc: EntityDesc<T>, entity: T, option: DeleteOption): Sql
    fun <T : Any> buildUpdate(entityDesc: EntityDesc<T>, entity: T, newEntity: T, option: UpdateOption): Sql
    fun <T : Any> buildMerge(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Sql

    fun <T : Any> buildUpsert(
        entityDesc: EntityDesc<T>,
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

    override fun <T : Any> buildFindById(entityDesc: EntityDesc<T>, id: Any, versionValue: Any?): Sql {
        with(entityDesc) {
            val buf = newSqlBuffer().append("select ")
            leafPropDescList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(" from $tableName where ")
            when (id) {
                is Collection<*> -> {
                    require(id.size == idList.size) { "The number of id must be {$idList.size}." }
                    id.zip(idList).forEach { (obj, propDesc) ->
                        val value = Value(obj, propDesc.kClass)
                        buf.append("${propDesc.columnName} = ").bind(value).append(" and ")
                    }
                }
                else -> {
                    require(idList.size == 1) { "The number of id must be ${idList.size}." }
                    val value = Value(id, idList[0].kClass)
                    buf.append("${idList[0].columnName} = ").bind(value).append(" and ")
                }
            }
            buf.cutBack(5)
            if (versionValue != null && version != null) {
                val value = Value(versionValue, version.kClass)
                buf.append(" and ").append("${version.columnName} = ").bind(value)
            }
            return buf.toSql()
        }
    }

    override fun <T : Any> buildInsert(entityDesc: EntityDesc<T>, newEntity: T, option: InsertOption): Sql {
        with(entityDesc) {
            val buf = newSqlBuffer().append("insert into $tableName (")
            val propDescList = leafPropDescList
                .filter { option.include.isEmpty() || it.prop in option.include }
                .filter { option.exclude.isEmpty() || it.prop !in option.exclude }
            propDescList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(") values (")
            propDescList.forEach {
                val value = it.getValue(newEntity as Any)
                buf.bind(value).append(", ")
            }
            buf.cutBack(2).append(")")
            return buf.toSql()
        }
    }

    override fun <T : Any> buildDelete(entityDesc: EntityDesc<T>, entity: T, option: DeleteOption): Sql {
        with(entityDesc) {
            val buf = newSqlBuffer()
            buf.append("delete from $tableName")
            buildWhereClauseForIdAndVersion(entityDesc, entity, option.ignoreVersion, buf)
            return buf.toSql()
        }
    }

    override fun <T : Any> buildUpdate(entityDesc: EntityDesc<T>, entity: T, newEntity: T, option: UpdateOption): Sql {
        with(entityDesc) {
            val buf = newSqlBuffer().append("update $tableName set ")
            nonIdList
                .filter { option.include.isEmpty() || it.prop in option.include }
                .filter { option.exclude.isEmpty() || it.prop !in option.exclude }
                .forEach {
                    val value = it.getValue(newEntity as Any)
                    buf.append("${it.columnName} = ").bind(value).append(", ")
                }
            buf.cutBack(2)
            buildWhereClauseForIdAndVersion(entityDesc, entity, option.ignoreVersion, buf)
            return buf.toSql()
        }
    }

    override fun <T : Any> buildMerge(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Sql {
        with(entityDesc) {
            val buf = newSqlBuffer().append("merge into $tableName using dual on ")
            if (keys.isNotEmpty()) {
                val keyPropDescList = keys.map { prop ->
                    propMap[prop] ?: error(
                        "The property \"${prop.name}\" is not found " +
                                "in the class \"${entityDesc.kClass.qualifiedName}\""
                    )
                }
                keyPropDescList.forEach {
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
            val propDescList = leafPropDescList
                .filter { insertOption.include.isEmpty() || it.prop in insertOption.include }
                .filter { insertOption.exclude.isEmpty() || it.prop !in insertOption.exclude }
            propDescList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(") values (")
            propDescList.forEach {
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

    override fun <T : Any> buildUpsert(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption

    ): Sql {
        with(entityDesc) {
            val buf = newSqlBuffer().append("insert into $tableName as t_ (")
            val propDescList = leafPropDescList
                .filter { insertOption.include.isEmpty() || it.prop in insertOption.include }
                .filter { insertOption.exclude.isEmpty() || it.prop !in insertOption.exclude }
            propDescList.forEach { buf.append("${it.columnName}, ") }
            buf.cutBack(2).append(") values(")
            propDescList.forEach {
                val value = it.getValue(newEntity)
                buf.bind(value).append(", ")
            }
            buf.cutBack(2).append(") on conflict (")
            if (keys.isNotEmpty()) {
                keys.map { prop ->
                    propMap[prop] ?: error(
                        "The property \"${prop.name}\" is not found " +
                                "in the class \"${entityDesc.kClass.qualifiedName}\""
                    )
                }.forEach { propDesc ->
                    buf.append("${propDesc.columnName}, ")
                }
            } else {
                idList.forEach { propDesc -> buf.append("${propDesc.columnName}, ") }
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

    protected open fun <T : Any> buildWhereClauseForIdAndVersion(
        entityDesc: EntityDesc<T>,
        entity: T,
        ignoreVersion: Boolean,
        buf: SqlBuffer
    ) {
        with(entityDesc) {
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

    protected fun <T : Any> PropDesc.getValue(entity: T): Value {
        val obj = this.deepGetter(entity)
        return Value(obj, this.kClass)
    }
}
