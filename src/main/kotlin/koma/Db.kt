package koma

import koma.meta.EntityMeta
import koma.meta.ObjectMeta
import koma.meta.makeEntityMeta
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

open class Db(config: DbConfig) {
    protected val dataSource = config.dataSource
    protected val dialect = config.dialect
    val transaction: TransactionScope by lazy { config.transactionScope }

    inline fun <reified T : Any> select(
        template: CharSequence,
        condition: Any = object {}
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        return `access$stream`(template, condition, T::class).use {
            it.collect(Collectors.toList())
        }
    }

    inline fun <reified T : Any> iterate(
        template: CharSequence,
        condition: Any = object {},
        action: (T) -> Unit
    ) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        return `access$stream`(template, condition, T::class).use {
            it.asSequence().forEach(action)
        }
    }

    inline fun <reified T : Any, R> sequence(
        template: CharSequence,
        condition: Any = object {},
        action: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        return `access$stream`(template, condition, T::class).use {
            action(it.asSequence())
        }
    }

    @PublishedApi
    internal fun <T : Any> `access$stream`(template: CharSequence, condition: Any, clazz: KClass<T>) =
        stream(template, condition, clazz)

    protected fun <T : Any> stream(
        template: CharSequence,
        condition: Any = object {},
        clazz: KClass<T>
    ): Stream<T> {
        require(clazz.isData) { "The clazz must be a data class." }
        require(!clazz.isAbstract) { "The clazz must not be abstract." }
        val meta = makeEntityMeta(clazz)
        return executeQuery(template.toString(), condition) { rs ->
            val paramMap = mutableMapOf<Int, KParameter>()
            val metaData = rs.metaData
            val count = metaData.columnCount
            for (i in 1..count) {
                val label = metaData.getColumnLabel(i).toLowerCase()
                val param = meta.consParamMap[label] ?: continue
                paramMap[i] = param
            }
            stream(rs) {
                val row = mutableMapOf<KParameter, Any?>()
                for ((index, param) in paramMap) {
                    val value = dialect.getValue(it, index, param.type.jvmErasure)
                    row[param] = value
                }
                meta.new(row)
            }
        }
    }

    inline fun <reified T : Any?> selectOneColumn(
        template: CharSequence,
        condition: Any = object {}
    ): List<T> {
        return `access$streamOneColumn`<T>(template, condition, T::class).use {
            it.collect(Collectors.toList())
        }
    }

    inline fun <reified T : Any?> iterateOneColumn(
        template: CharSequence,
        condition: Any = object {},
        action: (T) -> Unit
    ) {
        `access$streamOneColumn`<T>(template, condition, T::class).use {
            it.asSequence().forEach(action)
        }
    }

    inline fun <reified T : Any?, R> sequenceOneColumn(
        template: CharSequence,
        condition: Any = object {},
        action: (Sequence<T>) -> R
    ): R {
        return `access$streamOneColumn`<T>(template, condition, T::class).use {
            action(it.asSequence())
        }
    }

    @PublishedApi
    internal fun <T> `access$streamOneColumn`(template: CharSequence, condition: Any, type: KClass<*>) =
        streamOneColumn<T>(template, condition, type)

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any?> streamOneColumn(
        template: CharSequence,
        condition: Any = object {},
        type: KClass<*>
    ): Stream<T> {
        return executeQuery(template.toString(), condition) { rs ->
            stream(rs) {
                dialect.getValue(it, 1, type) as T
            }
        }
    }

    protected fun <T> stream(rs: ResultSet, provider: (ResultSet) -> T): Stream<T> {
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

    protected fun <T : Any?> executeQuery(
        template: String,
        condition: Any,
        handler: (rs: ResultSet) -> Stream<T>
    ): Stream<T> {
        val objectMeta = ObjectMeta(condition::class)
        val ctx = objectMeta.toMap(condition)
        val sql = SqlBuilder().build(template, ctx)
        var stream: Stream<T>? = null
        val con = dataSource.connection
        try {
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

    inline fun <reified T : Any> insert(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = makeEntityMeta(T::class)
        return `access$insert`(entity, meta)
    }

    @PublishedApi
    internal fun <T : Any> `access$insert`(entity: T, meta: EntityMeta<T>) = insert(entity, meta)

    protected fun <T : Any> insert(entity: T, meta: EntityMeta<T>): T {
        return meta.assignId(entity).also { newEntity ->
            val sql = meta.buildInsertSql(newEntity)
            val count = try {
                executeUpdate(sql)
            } catch (e: SQLException) {
                if (dialect.isUniqueConstraintViolated(e)) {
                    throw UniqueConstraintException(e, entity)
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
        val meta = makeEntityMeta(T::class)
        `access$delete`(entity, meta)
    }

    @PublishedApi
    internal fun <T : Any> `access$delete`(entity: T, meta: EntityMeta<T>) = delete(entity, meta)

    protected fun <T : Any> delete(entity: T, meta: EntityMeta<T>) {
        val sql = meta.buildDeleteSql(entity)
        val count = executeUpdate(sql)
        if (count == 0 && meta.versionPropMeta != null) {
            throw OptimisticLockException(entity)
        }
    }

    inline fun <reified T : Any> update(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = makeEntityMeta(T::class)
        return `access$update`(entity, meta)
    }

    @PublishedApi
    internal fun <T : Any> `access$update`(entity: T, meta: EntityMeta<T>) = update(entity, meta)

    protected fun <T : Any> update(entity: T, meta: EntityMeta<T>): T {
        return meta.incrementVersion(entity).also { newEntity ->
            val sql = meta.buildUpdateSql(entity, newEntity)
            val count = try {
                executeUpdate(sql)
            } catch (e: SQLException) {
                if (dialect.isUniqueConstraintViolated(e)) {
                    throw UniqueConstraintException(e, entity)
                } else {
                    throw e
                }
            }
            if (count == 0 && meta.versionPropMeta != null) {
                throw OptimisticLockException(entity)
            }
        }
    }

    protected fun executeUpdate(sql: Sql): Int {
        return dataSource.connection.use { con ->
            con.prepareStatement(sql.text).use { stmt ->
                bindValues(stmt, sql.values)
                stmt.executeUpdate()
            }
        }
    }

    protected fun bindValues(stmt: PreparedStatement, values: List<Pair<Any?, KClass<*>>>) {
        values.forEachIndexed { index, (value, valueType) ->
            dialect.setValue(stmt, index + 1, value, valueType)
        }
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
}

class OptimisticLockException(val entity: Any) : Exception()

class UniqueConstraintException(cause: SQLException, val entity: Any) : Exception(cause)
