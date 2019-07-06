package org.komapper.core

import org.komapper.core.criteria.CriteriaProcessor
import org.komapper.core.criteria.CriteriaScope
import org.komapper.core.criteria.MultiEntityMeta
import org.komapper.core.meta.EntityMeta
import org.komapper.core.meta.PropMeta
import org.komapper.core.sql.Sql
import org.komapper.core.tx.TransactionScope
import org.komapper.core.value.Value
import java.sql.*
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.streams.asSequence
import kotlin.streams.toList

class Db(val config: DbConfig) {

    val transaction: TransactionScope
        get() = config.transactionScope

    inline fun <reified T : Any> findById(id: Any, version: Any? = null): T? {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = config.entityMetaFactory.get(T::class)
        val sql = config.entitySqlBuilder.buildFindById(meta, id, version)
        return `access$streamEntity`(sql, meta).use { stream ->
            stream.toList().firstOrNull()
        }
    }

    inline fun <reified T : Any> select(
        criteriaBlock: CriteriaScope<T>.() -> Unit = { }
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        return select(criteriaBlock, Sequence<T>::toList)
    }

    inline fun <reified T : Any, R> select(
        criteriaBlock: CriteriaScope<T>.() -> Unit = { },
        sequenceBlock: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val scope = CriteriaScope(T::class).also { it.criteriaBlock() }
        val processor = CriteriaProcessor(config.dialect, config.entityMetaFactory, scope())
        val sql = processor.buildSelect()
        return `access$streamMultiEntity`(sql, processor).use { stream ->
            stream.asSequence().map { entities ->
                val entity = entities.first()
                val joinedEntities = entities.subList(1, entities.size)
                processor.associate(entity, joinedEntities)
                entity as T
            }.let { sequenceBlock(it) }
        }
    }

    inline fun <reified T : Any> query(
        template: CharSequence,
        condition: Any? = null
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        return query(template, condition, Sequence<T>::toList)
    }

    inline fun <reified T : Any, R> query(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = config.entityMetaFactory.get(T::class)
        val ctx = config.objectMetaFactory.toMap(condition)
        val sql = config.sqlBuilder.build(template, ctx, meta.expander)
        return `access$streamEntity`(sql, meta).use { stream ->
            block(stream.asSequence())
        }
    }

    inline fun <reified T : Any?> queryOneColumn(
        template: CharSequence,
        condition: Any? = null
    ): List<T> = queryOneColumn(template, condition, Sequence<T>::toList)

    inline fun <reified T : Any?, R> queryOneColumn(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<T>) -> R
    ): R {
        val ctx = config.objectMetaFactory.toMap(condition)
        val sql = config.sqlBuilder.build(template, ctx)
        return `access$streamOneColumn`<T>(sql, T::class).use { stream ->
            block(stream.asSequence())
        }
    }

    inline fun <reified A : Any?, reified B : Any?> queryTwoColumns(
        template: CharSequence,
        condition: Any? = null
    ): List<Pair<A, B>> = queryTwoColumns(template, condition, Sequence<Pair<A, B>>::toList)

    inline fun <reified A : Any?, reified B : Any?, R> queryTwoColumns(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<Pair<A, B>>) -> R
    ): R {
        val ctx = config.objectMetaFactory.toMap(condition)
        val sql = config.sqlBuilder.build(template, ctx)
        return `access$streamTwoColumns`<A, B>(sql, A::class, B::class).use { stream ->
            block(stream.asSequence())
        }
    }

    inline fun <reified A : Any?, reified B : Any?, reified C : Any?> queryThreeColumns(
        template: CharSequence,
        condition: Any? = null
    ): List<Triple<A, B, C>> = queryThreeColumns(template, condition, Sequence<Triple<A, B, C>>::toList)

    inline fun <reified A : Any?, reified B : Any?, reified C : Any?, R> queryThreeColumns(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<Triple<A, B, C>>) -> R
    ): R {
        val ctx = config.objectMetaFactory.toMap(condition)
        val sql = config.sqlBuilder.build(template, ctx)
        return `access$streamThreeColumns`<A, B, C>(sql, A::class, B::class, C::class).use { stream ->
            block(stream.asSequence())
        }
    }

    inline fun <reified T : Any> paginate(
        template: CharSequence,
        condition: Any? = null,
        limit: Int?,
        offset: Int?
    ): Pair<List<T>, Int> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val paginationTemplate = config.sqlRewriter.rewriteForPagination(template, limit, offset)
        val countTemplate = config.sqlRewriter.rewriteForCount(template)
        val list = query<T>(paginationTemplate, condition)
        val count = queryOneColumn<Int>(countTemplate, condition).first()
        return list to count
    }

    private fun streamMultiEntity(
        sql: Sql,
        meta: MultiEntityMeta
    ): Stream<List<Any>> = executeQuery(sql) { rs ->
        fromResultSetToStream(rs) {
            val row = mutableMapOf<PropMeta<*, *>, Any?>()
            for ((i, propMeta) in meta.leafPropMetaList.withIndex()) {
                val value = config.dialect.getValue(it, i + 1, propMeta.type)
                row[propMeta] = value
            }
            meta.new(row)
        }
    }

    private fun <T : Any> streamEntity(
        sql: Sql,
        meta: EntityMeta<T>
    ): Stream<T> = executeQuery(sql) { rs ->
        val propMetaMap = mutableMapOf<Int, PropMeta<*, *>>()
        val metaData = rs.metaData
        val count = metaData.columnCount
        for (i in 1..count) {
            val label = metaData.getColumnLabel(i).toLowerCase()
            val propMeta = meta.columnLabelMap[label] ?: continue
            propMetaMap[i] = propMeta
        }
        fromResultSetToStream(rs) {
            val row = mutableMapOf<PropMeta<*, *>, Any?>()
            for ((index, propMeta) in propMetaMap) {
                val value = config.dialect.getValue(it, index, propMeta.type)
                row[propMeta] = value
            }
            meta.new(row)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any?> streamOneColumn(
        sql: Sql,
        type: KClass<*>
    ): Stream<T> = executeQuery(sql) { rs ->
        fromResultSetToStream(rs) {
            config.dialect.getValue(it, 1, type) as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Any?, B : Any?> streamTwoColumns(
        sql: Sql,
        firstType: KClass<*>,
        secondType: KClass<*>
    ): Stream<Pair<A, B>> = executeQuery(sql) { rs ->
        fromResultSetToStream(rs) {
            val first = config.dialect.getValue(it, 1, firstType) as A
            val second = config.dialect.getValue(it, 2, secondType) as B
            first to second
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : Any?, B : Any?, C : Any?> streamThreeColumns(
        sql: Sql,
        firstType: KClass<*>,
        secondType: KClass<*>,
        thirdType: KClass<*>
    ): Stream<Triple<A, B, C>> = executeQuery(sql) { rs ->
        fromResultSetToStream(rs) {
            val first = config.dialect.getValue(it, 1, firstType) as A
            val second = config.dialect.getValue(it, 2, secondType) as B
            val third = config.dialect.getValue(it, 3, thirdType) as C
            Triple(first, second, third)
        }
    }

    private fun <T : Any?> executeQuery(
        sql: Sql,
        handler: (rs: ResultSet) -> Stream<T>
    ): Stream<T> {
        var stream: Stream<T>? = null
        val con = config.connection
        try {
            val ps = con.prepareStatement(sql.text)
            try {
                log(sql)
                ps.setUp()
                ps.bind(sql.values)
                val rs = ps.executeQuery()
                try {
                    return handler(rs).also { stream = it }
                } finally {
                    stream.onClose(rs)
                }
            } finally {
                stream.onClose(ps)
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

    inline fun <reified T : Any> insert(entity: T, option: InsertOption = InsertOption()): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = config.entityMetaFactory.get(T::class)
        return if (option.assignId) {
            meta.assignId(entity, config.name) { sequenceName ->
                queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
            }
        } else {
            entity
        }.let { newEntity ->
            if (option.assignTimestamp) meta.assignTimestamp(newEntity) else newEntity
        }.let { newEntity ->
            config.listener.preInsert(newEntity, meta)
        }.let { newEntity ->
            val sql = config.entitySqlBuilder.buildInsert(meta, newEntity, option)
            val count = try {
                `access$executeUpdate`(sql)
            } catch (e: SQLException) {
                if (config.dialect.isUniqueConstraintViolation(e)) {
                    throw UniqueConstraintException(e)
                } else {
                    throw e
                }
            }
            check(count == 1)
            config.listener.postInsert(newEntity, meta)
        }
    }

    inline fun <reified T : Any> delete(entity: T, option: DeleteOption = DeleteOption()): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = config.entityMetaFactory.get(T::class)
        return config.listener.preDelete(entity, meta).let { newEntity ->
            val sql = config.entitySqlBuilder.buildDelete(meta, newEntity, option)
            val count = `access$executeUpdate`(sql)
            if (!option.ignoreVersion && meta.version != null && count != 1) {
                throw OptimisticLockException()
            }
            config.listener.postDelete(newEntity, meta)
        }
    }

    inline fun <reified T : Any> update(entity: T, option: UpdateOption = UpdateOption()): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = config.entityMetaFactory.get(T::class)
        return (if (option.incrementVersion) meta.incrementVersion(entity) else entity).let { newEntity ->
            if (option.updateTimestamp) meta.updateTimestamp(newEntity) else newEntity
        }.let { newEntity ->
            config.listener.preUpdate(newEntity, meta)
        }.let { newEntity ->
            val sql = config.entitySqlBuilder.buildUpdate(meta, entity, newEntity, option)
            val count = try {
                `access$executeUpdate`(sql)
            } catch (e: SQLException) {
                if (config.dialect.isUniqueConstraintViolation(e)) {
                    throw UniqueConstraintException(e)
                } else {
                    throw e
                }
            }
            if (!option.ignoreVersion && meta.version != null && count != 1) {
                throw OptimisticLockException()
            }
            config.listener.postUpdate(newEntity, meta)
        }
    }

    inline fun <reified T : Any> merge(
        entity: T,
        vararg keys: KProperty1<*, *>,
        insertOption: InsertOption = InsertOption(
            assignId = false,
            assignTimestamp = false
        ),
        updateOption: UpdateOption = UpdateOption(
            incrementVersion = false,
            updateTimestamp = false,
            ignoreVersion = true
        )
    ): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = config.entityMetaFactory.get(T::class)
        return if (insertOption.assignId) {
            meta.assignId(entity, config.name) { sequenceName ->
                queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
            }
        } else {
            entity
        }.let { newEntity ->
            if (insertOption.assignTimestamp) meta.assignTimestamp(newEntity) else newEntity
        }.let { newEntity ->
            if (updateOption.incrementVersion) meta.incrementVersion(newEntity) else newEntity
        }.let { newEntity ->
            if (updateOption.incrementVersion) meta.incrementVersion(newEntity) else newEntity
        }.let { newEntity ->
            if (updateOption.updateTimestamp) meta.updateTimestamp(newEntity) else newEntity
        }.let { newEntity ->
            config.listener.preMerge(newEntity, meta)
        }.let { newEntity ->
            val buildMerge: (EntityMeta<T>, T, T, List<KProperty1<*, *>>, InsertOption, UpdateOption) -> Sql = when {
                config.dialect.supportsMerge() -> config.entitySqlBuilder::buildMerge
                config.dialect.supportsUpsert() -> config.entitySqlBuilder::buildUpsert
                else -> error("The merge command is not supported.")
            }
            val sql = buildMerge(meta, entity, newEntity, keys.toList(), insertOption, updateOption)
            val count = try {
                `access$executeUpdate`(sql)
            } catch (e: SQLException) {
                if (config.dialect.isUniqueConstraintViolation(e)) {
                    throw UniqueConstraintException(e)
                } else {
                    throw e
                }
            }
            if (!updateOption.ignoreVersion && meta.version != null && count != 1) {
                throw OptimisticLockException()
            }
            config.listener.postMerge(newEntity, meta)
        }
    }

    private fun executeUpdate(sql: Sql): Int {
        return config.connection.use { con ->
            con.prepareStatement(sql.text).use { ps ->
                log(sql)
                ps.setUp()
                ps.bind(sql.values)
                ps.executeUpdate()
            }
        }
    }

    inline fun <reified T : Any> batchInsert(entities: List<T>, option: InsertOption = InsertOption()): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return entities
        val meta = config.entityMetaFactory.get(T::class)
        val size = entities.size
        val (newEntities, sqls) = entities.map { entity ->
            if (option.assignId) {
                meta.assignId(entity, config.name) { sequenceName ->
                    queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
                }
            } else {
                entity
            }.let { newEntity ->
                if (option.assignTimestamp) meta.assignTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                config.listener.preInsert(newEntity, meta)
            }.let { newEntity ->
                newEntity to config.entitySqlBuilder.buildInsert(meta, newEntity, option)
            }
        }.fold(ArrayList<T>(size) to ArrayList<Sql>(size)) { acc, (e, s) ->
            acc.also { it.first.add(e); it.second.add(s) }
        }
        val counts = try {
            `access$executeBatch`(sqls)
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolation(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        check(counts.all { it == 1 })
        return newEntities.map { config.listener.postInsert(it, meta) }
    }

    inline fun <reified T : Any> batchDelete(entities: List<T>, option: DeleteOption = DeleteOption()): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return entities
        val meta = config.entityMetaFactory.get(T::class)
        val size = entities.size
        val (newEntities, sqls) = entities.map { entity ->
            config.listener.preDelete(entity, meta).let { newEntity ->
                newEntity to config.entitySqlBuilder.buildDelete(meta, newEntity, option)
            }
        }.fold(ArrayList<T>(size) to ArrayList<Sql>(size)) { acc, (e, s) ->
            acc.also { it.first.add(e); it.second.add(s) }
        }
        val counts = `access$executeBatch`(sqls)
        if (!option.ignoreVersion && meta.version != null && counts.any { it != 1 }) {
            throw OptimisticLockException()
        }
        return newEntities.map { config.listener.postDelete(it, meta) }
    }

    inline fun <reified T : Any> batchUpdate(entities: List<T>, option: UpdateOption = UpdateOption()): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return entities
        val meta = config.entityMetaFactory.get(T::class)
        val size = entities.size
        val (newEntities, sqls) = entities.map { entity ->
            (if (option.incrementVersion) meta.incrementVersion(entity) else entity).let { newEntity ->
                if (option.updateTimestamp) meta.updateTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                config.listener.preUpdate(newEntity, meta)
            }.let { newEntity ->
                newEntity to config.entitySqlBuilder.buildUpdate(meta, entity, newEntity, option)
            }
        }.fold(ArrayList<T>(size) to ArrayList<Sql>(size)) { acc, (e, s) ->
            acc.also { it.first.add(e); it.second.add(s) }
        }
        val counts = try {
            `access$executeBatch`(sqls)
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolation(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        if (!option.ignoreVersion && meta.version != null && counts.any { it != 1 }) {
            throw OptimisticLockException()
        }
        return newEntities.map { config.listener.postUpdate(it, meta) }
    }

    inline fun <reified T : Any> batchMerge(
        entities: List<T>, vararg keys: KProperty1<*, *>,
        insertOption: InsertOption = InsertOption(
            assignId = false,
            assignTimestamp = false
        ),
        updateOption: UpdateOption = UpdateOption(
            incrementVersion = false,
            updateTimestamp = false,
            ignoreVersion = true
        )
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        if (entities.isEmpty()) return entities
        val meta = config.entityMetaFactory.get(T::class)
        val size = entities.size
        val (newEntities, sqls) = entities.map { entity ->
            if (insertOption.assignId) {
                meta.assignId(entity, config.name) { sequenceName ->
                    queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
                }
            } else {
                entity
            }.let { newEntity ->
                if (insertOption.assignTimestamp) meta.assignTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                if (updateOption.incrementVersion) meta.incrementVersion(newEntity) else newEntity
            }.let { newEntity ->
                if (updateOption.incrementVersion) meta.incrementVersion(newEntity) else newEntity
            }.let { newEntity ->
                if (updateOption.updateTimestamp) meta.updateTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                config.listener.preMerge(newEntity, meta)
            }.let { newEntity ->
                val buildMerge: (EntityMeta<T>, T, T, List<KProperty1<*, *>>, InsertOption, UpdateOption) -> Sql =
                    when {
                        config.dialect.supportsMerge() -> config.entitySqlBuilder::buildMerge
                        config.dialect.supportsUpsert() -> config.entitySqlBuilder::buildUpsert
                        else -> error("The merge command is not supported.")
                    }
                newEntity to buildMerge(meta, entity, newEntity, keys.toList(), insertOption, updateOption)
            }
        }.fold(ArrayList<T>(size) to ArrayList<Sql>(size)) { acc, (e, s) ->
            acc.also { it.first.add(e); it.second.add(s) }
        }
        val counts = try {
            `access$executeBatch`(sqls)
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolation(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        if (!updateOption.ignoreVersion && meta.version != null && counts.any { it != 1 }) {
            throw OptimisticLockException()
        }
        return newEntities.map { config.listener.postMerge(it, meta) }
    }

    private fun executeBatch(sqls: Collection<Sql>): IntArray {
        return config.connection.use { con ->
            con.prepareStatement(sqls.first().text).use { ps ->
                val batchSize = config.batchSize
                val allCounts = IntArray(sqls.size)
                var offset = 0
                for ((i, sql) in sqls.withIndex()) {
                    log(sql)
                    ps.setUp()
                    ps.bind(sql.values)
                    ps.addBatch()
                    if (i == sqls.size - 1 || batchSize > 0 && (i + 1) % batchSize == 0) {
                        val counts = ps.executeBatch()
                        counts.copyInto(allCounts, offset)
                        offset = i + 1
                    }
                }
                allCounts
            }
        }
    }

    fun executeUpdate(template: CharSequence, condition: Any? = null): Int {
        val ctx = config.objectMetaFactory.toMap(condition)
        val sql = config.sqlBuilder.build(template, ctx)
        return executeUpdate(sql)
    }

    fun execute(statements: CharSequence) {
        executeUpdate(Sql(statements.toString(), emptyList(), null))
    }

    fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array = config.connection.use {
        it.createArrayOf(typeName, elements.toTypedArray())
    }

    fun createBlob(): Blob = config.connection.use {
        it.createBlob()
    }

    fun createClob(): Clob = config.connection.use {
        it.createClob()
    }

    fun createNClob(): NClob = config.connection.use {
        it.createNClob()
    }

    fun createSQLXML(): SQLXML = config.connection.use {
        it.createSQLXML()
    }

    private fun log(sql: Sql) = config.logger.logSql(sql)

    private fun PreparedStatement.setUp() {
        config.fetchSize?.let { if (it > 0) this.fetchSize = it }
        config.maxRows?.let { if (it > 0) this.maxRows = it }
        config.queryTimeout?.let { if (it > 0) this.queryTimeout = it }
    }

    private fun PreparedStatement.bind(values: List<Value>) {
        values.forEachIndexed { index, (obj, type) ->
            config.dialect.setValue(this, index + 1, obj, type)
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

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun `access$streamMultiEntity`(
        sql: Sql,
        meta: MultiEntityMeta
    ) = streamMultiEntity(sql, meta)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T : Any> `access$streamEntity`(
        sql: Sql,
        meta: EntityMeta<T>
    ) = streamEntity(sql, meta)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T : Any?> `access$streamOneColumn`(
        sql: Sql, type: KClass<*>
    ) = streamOneColumn<T>(sql, type)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <A : Any?, B : Any?> `access$streamTwoColumns`(
        sql: Sql,
        firstType: KClass<*>,
        secondType: KClass<*>
    ) = streamTwoColumns<A, B>(sql, firstType, secondType)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <A : Any?, B : Any?, C : Any?> `access$streamThreeColumns`(
        sql: Sql,
        firstType: KClass<*>,
        secondType: KClass<*>,
        thirdType: KClass<*>
    ) = streamThreeColumns<A, B, C>(sql, firstType, secondType, thirdType)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun `access$executeUpdate`(sql: Sql) = executeUpdate(sql)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun `access$executeBatch`(sqls: Collection<Sql>) = executeBatch(sqls)

}
