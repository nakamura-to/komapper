package koma

import koma.meta.EntityMeta
import koma.meta.emptyObject
import koma.meta.getEntityMeta
import koma.meta.toMap
import koma.sql.Sql
import koma.sql.SqlBuilder
import koma.tx.TransactionScope
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure
import kotlin.streams.asSequence
import kotlin.streams.toList

class Db(val config: DbConfig) {
    val transaction: TransactionScope
        get() = config.transactionScope

    inline fun <reified T : Any> findById(id: Any, version: Any? = null): T? {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        val sql = meta.buildFindByIdSql(id, version)
        return accessStream(sql, meta).toList().firstOrNull()
    }

    inline fun <reified T : Any> select(
        template: CharSequence,
        condition: Any = emptyObject
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        val ctx = toMap(condition)
        val sql = SqlBuilder().build(template, ctx)
        return accessStream(sql, meta).use {
            it.collect(Collectors.toList())
        }
    }

    inline fun <reified T : Any> iterate(
        template: CharSequence,
        condition: Any = emptyObject,
        action: (T) -> Unit
    ) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        val ctx = toMap(condition)
        val sql = SqlBuilder().build(template, ctx)
        return accessStream(sql, meta).use {
            it.asSequence().forEach(action)
        }
    }

    inline fun <reified T : Any, R> sequence(
        template: CharSequence,
        condition: Any = emptyObject,
        block: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        val ctx = toMap(condition)
        val sql = SqlBuilder().build(template, ctx)
        return accessStream(sql, meta).use {
            block(it.asSequence())
        }
    }

    @PublishedApi
    internal fun <T : Any> accessStream(sql: Sql, meta: EntityMeta<T>) = stream(sql, meta)

    private fun <T : Any> stream(
        sql: Sql,
        meta: EntityMeta<T>
    ): Stream<T> {
        return executeQuery(sql) { rs ->
            val paramMap = mutableMapOf<Int, KParameter>()
            val metaData = rs.metaData
            val count = metaData.columnCount
            for (i in 1..count) {
                val label = metaData.getColumnLabel(i).toLowerCase()
                val param = meta.consParamMap[label] ?: continue
                paramMap[i] = param
            }
            fromResultSetToStream(rs) {
                val row = mutableMapOf<KParameter, Any?>()
                for ((index, param) in paramMap) {
                    val value = config.dialect.getValue(it, index, param.type.jvmErasure)
                    row[param] = value
                }
                meta.new(row)
            }
        }
    }

    inline fun <reified T : Any?> selectOneColumn(
        template: CharSequence,
        condition: Any = emptyObject
    ): List<T> {
        return accessStreamOneColumn<T>(template, condition, T::class).use {
            it.collect(Collectors.toList())
        }
    }

    inline fun <reified T : Any?> iterateOneColumn(
        template: CharSequence,
        condition: Any = emptyObject,
        action: (T) -> Unit
    ) {
        accessStreamOneColumn<T>(template, condition, T::class).use {
            it.asSequence().forEach(action)
        }
    }

    inline fun <reified T : Any?, R> sequenceOneColumn(
        template: CharSequence,
        condition: Any = emptyObject,
        block: (Sequence<T>) -> R
    ): R {
        return accessStreamOneColumn<T>(template, condition, T::class).use {
            block(it.asSequence())
        }
    }

    @PublishedApi
    internal fun <T> accessStreamOneColumn(template: CharSequence, condition: Any, type: KClass<*>) =
        streamOneColumn<T>(template, condition, type)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any?> streamOneColumn(
        template: CharSequence,
        condition: Any = emptyObject,
        type: KClass<*>
    ): Stream<T> {
        val ctx = toMap(condition)
        val sql = SqlBuilder().build(template.toString(), ctx)
        return executeQuery(sql) { rs ->
            fromResultSetToStream(rs) {
                config.dialect.getValue(it, 1, type) as T
            }
        }
    }

    private fun <T : Any?> executeQuery(
        sql: Sql,
        handler: (rs: ResultSet) -> Stream<T>
    ): Stream<T> {
        var stream: Stream<T>? = null
        val con = config.dataSource.connection
        try {
            log(sql)
            val stmt = con.prepareStatement(sql.text)
            try {
                bindValues(stmt, sql.values)
                val rs = stmt.executeQuery()
                try {
                    return handler(rs).also { stream = it }
                } finally {
                    stream.onClose(rs)
                }
            } finally {
                stream.onClose(stmt)
            }
        } finally {
            stream.onClose(con)
        }
    }

    private fun <T> fromResultSetToStream(rs: ResultSet, provider: (ResultSet) -> T): Stream<T> {
        val iterator = object : Iterator<T> {
            var hasNext = rs.next()
            override fun hasNext(): Boolean {
                return hasNext
            }

            override fun next(): T {
                return provider(rs).also { hasNext = rs.next() }
            }
        }
        return StreamSupport.stream(
            { Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED) },
            Spliterator.ORDERED,
            false
        )
    }

    private fun <T : Any?> Stream<T>?.onClose(closeable: AutoCloseable) {
        if (this == null) {
            try {
                closeable.close()
            } catch (ignored: Exception) {
            }
        } else {
            onClose(closeable::close)
        }
    }

    inline fun <reified T : Any> insert(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        return meta.assignId(entity, config.name) { sequenceName ->
            selectOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
        }.also { newEntity ->
            val sql = meta.buildInsertSql(newEntity)
            val count = try {
                accessExecuteUpdate(sql)
            } catch (e: SQLException) {
                if (config.dialect.isUniqueConstraintViolated(e)) {
                    throw UniqueConstraintException(e)
                } else {
                    throw e
                }
            }
            check(count == 1)
        }
    }

    inline fun <reified T : Any> delete(entity: T) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        val sql = meta.buildDeleteSql(entity)
        val count = accessExecuteUpdate(sql)
        if (count == 0 && meta.versionPropMeta != null) {
            throw OptimisticLockException()
        }
    }

    inline fun <reified T : Any> update(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class)
        return meta.incrementVersion(entity).also { newEntity ->
            val sql = meta.buildUpdateSql(entity, newEntity)
            val count = try {
                accessExecuteUpdate(sql)
            } catch (e: SQLException) {
                if (config.dialect.isUniqueConstraintViolated(e)) {
                    throw UniqueConstraintException(e)
                } else {
                    throw e
                }
            }
            if (count == 0 && meta.versionPropMeta != null) {
                throw OptimisticLockException()
            }
        }
    }

    fun executeUpdate(template: CharSequence, condition: Any = emptyObject): Int {
        val ctx = toMap(condition)
        val sql = SqlBuilder().build(template.toString(), ctx)
        return executeUpdate(sql)
    }

    fun execute(statements: CharSequence) {
        executeUpdate(Sql(statements.toString(), emptyList(), null))
    }

    @PublishedApi
    internal fun accessExecuteUpdate(sql: Sql) = executeUpdate(sql)

    private fun executeUpdate(sql: Sql): Int {
        return config.dataSource.connection.use { con ->
            log(sql)
            con.prepareStatement(sql.text).use { stmt ->
                bindValues(stmt, sql.values)
                stmt.executeUpdate()
            }
        }
    }

    private fun bindValues(stmt: PreparedStatement, values: List<Value>) {
        values.forEachIndexed { index, (value, valueType) ->
            config.dialect.setValue(stmt, index + 1, value, valueType)
        }
    }

    private fun log(sql: Sql) {
        sql.log?.let { log -> config.logger { log } }
    }

}
