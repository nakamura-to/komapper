package koma

import koma.meta.*
import koma.sql.Sql
import koma.sql.SqlBuilder
import koma.tx.TransactionScope
import java.sql.*
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.streams.asSequence
import kotlin.streams.toList

class Db(val config: DbConfig) {
    val transaction: TransactionScope
        get() = config.transactionScope

    inline fun <reified T : Any> findById(id: Any, version: Any? = null): T? {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val sql = meta.buildFindByIdSql(id, version)
        return `access$stream`(sql, meta).toList().firstOrNull()
    }

    inline fun <reified T : Any> select(
        template: CharSequence,
        condition: Any = emptyObject
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val ctx = toMap(condition)
        val sql = SqlBuilder(config.dialect::formatValue).build(template, ctx)
        return `access$stream`(sql, meta).use {
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
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val ctx = toMap(condition)
        val sql = SqlBuilder(config.dialect::formatValue).build(template, ctx)
        return `access$stream`(sql, meta).use {
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
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val ctx = toMap(condition)
        val sql = SqlBuilder(config.dialect::formatValue).build(template, ctx)
        return `access$stream`(sql, meta).use {
            block(it.asSequence())
        }
    }

    private fun <T : Any> stream(
        sql: Sql,
        meta: EntityMeta<T>
    ): Stream<T> {
        return executeQuery(sql) { rs ->
            val propMetaMap = mutableMapOf<Int, PropMeta<T>>()
            val metaData = rs.metaData
            val count = metaData.columnCount
            for (i in 1..count) {
                val label = metaData.getColumnLabel(i).toLowerCase()
                val propMeta = meta.propMetaMap[label] ?: continue
                propMetaMap[i] = propMeta
            }
            fromResultSetToStream(rs) {
                val row = mutableMapOf<KParameter, Any?>()
                for ((index, propMeta) in propMetaMap) {
                    val value = config.dialect.getValue(it, index, propMeta.type)
                    row[propMeta.consParam] = value
                }
                meta.new(row)
            }
        }
    }

    inline fun <reified T : Any?> selectOneColumn(
        template: CharSequence,
        condition: Any = emptyObject
    ): List<T> {
        return `access$streamOneColumn`<T>(template, condition, T::class).use {
            it.collect(Collectors.toList())
        }
    }

    inline fun <reified T : Any?> iterateOneColumn(
        template: CharSequence,
        condition: Any = emptyObject,
        action: (T) -> Unit
    ) {
        `access$streamOneColumn`<T>(template, condition, T::class).use {
            it.asSequence().forEach(action)
        }
    }

    inline fun <reified T : Any?, R> sequenceOneColumn(
        template: CharSequence,
        condition: Any = emptyObject,
        block: (Sequence<T>) -> R
    ): R {
        return `access$streamOneColumn`<T>(template, condition, T::class).use {
            block(it.asSequence())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any?> streamOneColumn(
        template: CharSequence,
        condition: Any = emptyObject,
        type: KClass<*>
    ): Stream<T> {
        val ctx = toMap(condition)
        val sql = SqlBuilder(config.dialect::formatValue).build(template.toString(), ctx)
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
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        return meta.assignId(entity, config.name) { sequenceName ->
            selectOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
        }.let { newEntity ->
            meta.assignTimestamp(newEntity)
        }.also { newEntity ->
            val sql = meta.buildInsertSql(newEntity)
            val count = try {
                `access$executeUpdate`(sql)
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
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val sql = meta.buildDeleteSql(entity)
        val count = `access$executeUpdate`(sql)
        if (meta.versionPropMeta != null && count != 1) {
            throw OptimisticLockException()
        }
        check(count == 1)
    }

    inline fun <reified T : Any> update(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        return meta.incrementVersion(entity).let { newEntity ->
            meta.updateTimestamp(newEntity)
        }.also { newEntity ->
            val sql = meta.buildUpdateSql(entity, newEntity)
            val count = try {
                `access$executeUpdate`(sql)
            } catch (e: SQLException) {
                if (config.dialect.isUniqueConstraintViolated(e)) {
                    throw UniqueConstraintException(e)
                } else {
                    throw e
                }
            }
            if (meta.versionPropMeta != null && count != 1) {
                throw OptimisticLockException()
            }
            check(count == 1)
        }
    }

    private fun executeUpdate(sql: Sql): Int {
        return config.dataSource.connection.use { con ->
            log(sql)
            con.prepareStatement(sql.text).use { stmt ->
                bindValues(stmt, sql.values)
                stmt.executeUpdate()
            }
        }
    }

    inline fun <reified T : Any> batchInsert(entities: Collection<T>) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val sqls = entities.map { entity ->
            meta.assignId(entity, config.name) { sequenceName ->
                selectOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
            }.let { newEntity ->
                meta.assignTimestamp(newEntity)
            }.let { newEntity ->
                meta.buildInsertSql(newEntity)
            }
        }
        val counts = try {
            `access$executeBatch`(sqls)
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolated(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        check(counts.all { it == 1 })
    }

    inline fun <reified T : Any> batchDelete(entities: Collection<T>) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val sqls = entities.map { meta.buildDeleteSql(it) }
        val counts = `access$executeBatch`(sqls)
        if (meta.versionPropMeta != null && counts.any { it != 1 }) {
            throw OptimisticLockException()
        }
        check(counts.all { it == 1 })
    }

    inline fun <reified T : Any> batchUpdate(entities: Collection<T>) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return
        val meta = getEntityMeta(T::class, config.dialect, config.namingStrategy)
        val sqls = entities.map { entity ->
            meta.incrementVersion(entity).let { newEntity ->
                meta.updateTimestamp(newEntity)
            }.let { newEntity ->
                meta.buildUpdateSql(entity, newEntity)
            }
        }
        val counts = try {
            `access$executeBatch`(sqls)
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolated(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        if (meta.versionPropMeta != null && counts.any { it != 1 }) {
            throw OptimisticLockException()
        }
        check(counts.all { it == 1 })
    }

    private fun executeBatch(sqls: Collection<Sql>): IntArray {
        return config.dataSource.connection.use { con ->
            con.prepareStatement(sqls.first().text).use { stmt ->
                val batchSize = config.batchSize
                val allCounts = IntArray(sqls.size)
                var offset = 0
                for ((i, sql) in sqls.withIndex()) {
                    log(sql)
                    bindValues(stmt, sql.values)
                    stmt.addBatch()
                    if (i == sqls.size - 1 || batchSize > 0 && (i + 1) % batchSize == 0) {
                        val counts = stmt.executeBatch()
                        counts.copyInto(allCounts, offset)
                        offset = i + 1
                    }
                }
                allCounts
            }
        }
    }

    fun executeUpdate(template: CharSequence, condition: Any = emptyObject): Int {
        val ctx = toMap(condition)
        val sql = SqlBuilder(config.dialect::formatValue).build(template.toString(), ctx)
        return executeUpdate(sql)
    }

    fun execute(statements: CharSequence) {
        executeUpdate(Sql(statements.toString(), emptyList(), null))
    }

    fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array {
        return config.dataSource.connection.use {
            it.createArrayOf(typeName, elements.toTypedArray())
        }
    }

    fun createBlob(): Blob {
        return config.dataSource.connection.use {
            it.createBlob()
        }
    }

    fun createClob(): Clob {
        return config.dataSource.connection.use {
            it.createClob()
        }
    }

    fun createNClob(): NClob {
        return config.dataSource.connection.use {
            it.createNClob()
        }
    }

    fun createSQLXML(): SQLXML {
        return config.dataSource.connection.use {
            it.createSQLXML()
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

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T : Any> `access$stream`(sql: Sql, meta: EntityMeta<T>) = stream(sql, meta)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T> `access$streamOneColumn`(template: CharSequence, condition: Any, type: KClass<*>) =
        streamOneColumn<T>(template, condition, type)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun `access$executeUpdate`(sql: Sql) = executeUpdate(sql)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun `access$executeBatch`(sqls: Collection<Sql>) = executeBatch(sqls)

}
