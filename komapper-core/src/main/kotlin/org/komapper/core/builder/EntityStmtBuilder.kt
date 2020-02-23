package org.komapper.core.builder

import kotlin.reflect.KProperty1
import org.komapper.core.DeleteOption
import org.komapper.core.InsertOption
import org.komapper.core.UpdateOption
import org.komapper.core.criteria.DeleteCriteria
import org.komapper.core.criteria.DeleteScope
import org.komapper.core.criteria.InsertCriteria
import org.komapper.core.criteria.InsertScope
import org.komapper.core.criteria.SelectCriteria
import org.komapper.core.criteria.SelectScope
import org.komapper.core.criteria.UpdateCriteria
import org.komapper.core.criteria.UpdateScope
import org.komapper.core.criteria.Where
import org.komapper.core.criteria.delete
import org.komapper.core.criteria.insert
import org.komapper.core.criteria.select
import org.komapper.core.criteria.update
import org.komapper.core.entity.EntityDesc
import org.komapper.core.entity.EntityDescFactory
import org.komapper.core.entity.PropDesc
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Stmt
import org.komapper.core.sql.StmtBuffer
import org.komapper.core.value.Value

interface EntityStmtBuilder {
    fun <T : Any> buildFindById(entityDesc: EntityDesc<T>, id: Any, versionValue: Any?): Stmt
    fun <T : Any> buildInsert(entityDesc: EntityDesc<T>, newEntity: T, option: InsertOption): Stmt
    fun <T : Any> buildDelete(entityDesc: EntityDesc<T>, entity: T, option: DeleteOption): Stmt
    fun <T : Any> buildUpdate(entityDesc: EntityDesc<T>, entity: T, newEntity: T, option: UpdateOption): Stmt
    fun <T : Any> buildMerge(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Stmt

    fun <T : Any> buildUpsert(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Stmt
}

open class DefaultEntityStmtBuilder(
    @Suppress("MemberVisibilityCanBePrivate")
    protected val dialect: Dialect,
    @Suppress("MemberVisibilityCanBePrivate")
    protected val entityDescFactory: EntityDescFactory

) : EntityStmtBuilder {

    override fun <T : Any> buildFindById(entityDesc: EntityDesc<T>, id: Any, versionValue: Any?): Stmt {
        val query = select<T> {
            where {
                with(entityDesc) {
                    when (id) {
                        is Collection<*> -> {
                            require(id.size == idList.size) { "The number of id must be {$idList.size}." }
                            id.zip(idList).forEach { (obj, propDesc) ->
                                eq(propDesc.prop, obj)
                            }
                        }
                        else -> {
                            require(idList.size == 1) { "The number of id must be ${idList.size}." }
                            eq(idList[0].prop, id)
                        }
                    }
                    if (versionValue != null && version != null) {
                        eq(version.prop, versionValue)
                    }
                }
            }
        }
        val criteria = SelectCriteria(entityDesc.kClass).also {
            SelectScope(it).query(it.alias)
        }
        val builder = SelectBuilder(dialect, entityDescFactory, criteria)
        return builder.build()
    }

    override fun <T : Any> buildInsert(entityDesc: EntityDesc<T>, newEntity: T, option: InsertOption): Stmt {
        val query = insert<T> {
            values {
                entityDesc.leafPropDescList
                    .filter { option.include.isEmpty() || it.prop in option.include }
                    .filter { option.exclude.isEmpty() || it.prop !in option.exclude }
                    .forEach {
                        value(it.prop, it.deepGetter(newEntity))
                    }
            }
        }
        val criteria = InsertCriteria(entityDesc.kClass).also {
            InsertScope(it).query(it.alias)
        }
        val builder = InsertBuilder(dialect, entityDescFactory, criteria)
        return builder.build()
    }

    override fun <T : Any> buildDelete(entityDesc: EntityDesc<T>, entity: T, option: DeleteOption): Stmt {
        val query = delete<T> {
            where(idAndVersionWhere(entityDesc, entity, option.ignoreVersion))
        }
        val criteria = DeleteCriteria(entityDesc.kClass).also {
            DeleteScope(it).query(it.alias)
        }
        val builder = DeleteBuilder(dialect, entityDescFactory, criteria)
        return builder.build()
    }

    override fun <T : Any> buildUpdate(entityDesc: EntityDesc<T>, entity: T, newEntity: T, option: UpdateOption): Stmt {
        val query = update<T> {
            set {
                entityDesc.nonIdList
                    .filter { option.include.isEmpty() || it.prop in option.include }
                    .filter { option.exclude.isEmpty() || it.prop !in option.exclude }
                    .forEach {
                        value(it.prop, it.deepGetter(newEntity))
                    }
            }
            where(idAndVersionWhere(entityDesc, entity, option.ignoreVersion))
        }
        val criteria = UpdateCriteria(entityDesc.kClass).also {
            UpdateScope(it).query(it.alias)
        }
        val builder = UpdateBuilder(dialect, entityDescFactory, criteria)
        return builder.build()
    }

    override fun <T : Any> buildMerge(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption
    ): Stmt {
        with(entityDesc) {
            val buf = newSqlBuffer().append("merge into $tableName using dual on ")
            if (keys.isNotEmpty()) {
                val keyPropDescList = keys.map { prop ->
                    propDescMapByProp[prop] ?: error(
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
            return buf.toStmt()
        }
    }

    override fun <T : Any> buildUpsert(
        entityDesc: EntityDesc<T>,
        entity: T,
        newEntity: T,
        keys: List<KProperty1<*, *>>,
        insertOption: InsertOption,
        updateOption: UpdateOption

    ): Stmt {
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
                    propDescMapByProp[prop] ?: error(
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
            return buf.toStmt()
        }
    }

    protected open fun <T : Any> idAndVersionWhere(
        entityDesc: EntityDesc<T>,
        entity: T,
        ignoreVersion: Boolean
    ): Where {
        return {
            with(entityDesc) {
                if (idList.isNotEmpty()) {
                    idList.forEach {
                        eq(it.prop, it.deepGetter(entity))
                    }
                }
                if (!ignoreVersion && version != null) {
                    eq(version.prop, version.deepGetter(entity))
                }
            }
        }
    }

    protected open fun newSqlBuffer(): StmtBuffer {
        return StmtBuffer(dialect::formatValue)
    }

    protected fun <T : Any> PropDesc.getValue(entity: T): Value {
        val obj = this.deepGetter(entity)
        return Value(obj, this.kClass)
    }
}
