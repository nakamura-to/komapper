package org.komapper.core

import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLXML
import java.util.ArrayList
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.streams.asSequence
import kotlin.streams.toList
import org.komapper.core.criteria.CriteriaProcessor
import org.komapper.core.criteria.CriteriaScope
import org.komapper.core.criteria.MultiEntityDesc
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.PropDesc
import org.komapper.core.sql.Sql
import org.komapper.core.tx.TransactionScope
import org.komapper.core.value.Value

/**
 * A database.
 *
 * @property config the database configuration
 * @constructor creates a database instance
 */
class Db(val config: DbConfig) {

    /**
     * A dry run.
     */
    val dryRun = DryRun(config)

    /**
     * Creates a transaction scope.
     */
    val transaction: TransactionScope
        get() = config.transactionScope

    /**
     * Finds an entity by Id and version.
     *
     * @param T the entity type
     * @param id the identifier (primary key)
     * @param version the version value
     * @return the found entity
     */
    inline fun <reified T : Any> findById(id: Any, version: Any? = null): T? {
        require(T::class.isData) { "The T must be a data class." }
        val (sql, desc) = dryRun.findById<T>(id, version)
        return `access$streamEntity`(sql, desc).use { stream ->
            stream.toList().firstOrNull()
        }
    }

    /**
     * Selects entities by criteria.
     *
     * @param T the entity type
     * @param criteriaBlock the criteria
     * @return the selected entities
     */
    inline fun <reified T : Any> select(
        criteriaBlock: CriteriaScope<T>.() -> Unit = { }
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        return select(criteriaBlock, Sequence<T>::toList)
    }

    /**
     * Selects entities by criteria and process them as sequence.
     *
     * @param T the entity type
     * @param criteriaBlock the criteria
     * @param sequenceBlock the processor
     * @return the processed result
     */
    inline fun <reified T : Any, R> select(
        criteriaBlock: CriteriaScope<T>.() -> Unit = { },
        sequenceBlock: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The T must be a data class." }
        val (sql, desc) = dryRun.select(criteriaBlock)
        return `access$streamMultiEntity`(sql, desc).use { stream ->
            stream.asSequence().map { entities ->
                val entity = entities.first()
                val joinedEntities = entities.subList(1, entities.size)
                desc.associate(entity, joinedEntities)
                entity as T
            }.let { sequenceBlock(it) }
        }
    }

    /**
     * Queries entities by the SQL template.
     *
     * @param T the entity type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @return the queried entities
     */
    inline fun <reified T : Any> query(
        template: CharSequence,
        condition: Any? = null
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        return query(template, condition, Sequence<T>::toList)
    }

    /**
     * Queries entities by the SQL template and process them as sequence.
     *
     * @param T the entity type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @param block the processor
     * @return the processed result
     */
    inline fun <reified T : Any, R> query(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The T must be a data class." }
        val (sql, desc) = dryRun.query<T>(template, condition)
        return `access$streamEntity`(sql, desc).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Queries one column values by the SQL template.
     *
     * @param T the column type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @return the queried column values
     */
    inline fun <reified T : Any?> queryOneColumn(
        template: CharSequence,
        condition: Any? = null
    ): List<T> = queryOneColumn(template, condition, Sequence<T>::toList)

    /**
     * Queries one column by the SQL template and process them as sequence.
     *
     * @param T the column type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @param block the processor
     * @return the processed result
     */
    inline fun <reified T : Any?, R> queryOneColumn(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<T>) -> R
    ): R {
        val sql = dryRun.queryOneColumn(template, condition)
        return `access$streamOneColumn`<T>(sql, T::class).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Queries two columns by the SQL template.
     *
     * @param A the first column type
     * @param B the second column type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @return the queried column values
     */
    inline fun <reified A : Any?, reified B : Any?> queryTwoColumns(
        template: CharSequence,
        condition: Any? = null
    ): List<Pair<A, B>> = queryTwoColumns(template, condition, Sequence<Pair<A, B>>::toList)

    /**
     * Queries two column by the SQL template and process them as sequence.
     *
     * @param A the first column type
     * @param B the second column type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @param block the processor
     * @return the processed result
     */
    inline fun <reified A : Any?, reified B : Any?, R> queryTwoColumns(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<Pair<A, B>>) -> R
    ): R {
        val sql = dryRun.queryTwoColumns(template, condition)
        return `access$streamTwoColumns`<A, B>(sql, A::class, B::class).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Queries three columns by the SQL template.
     *
     * @param A the first column type
     * @param B the second column type
     * @param C the third column type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @return the queried column values
     */
    inline fun <reified A : Any?, reified B : Any?, reified C : Any?> queryThreeColumns(
        template: CharSequence,
        condition: Any? = null
    ): List<Triple<A, B, C>> = queryThreeColumns(template, condition, Sequence<Triple<A, B, C>>::toList)

    /**
     * Queries three column by the SQL template and process them as sequence.
     *
     * @param A the first column type
     * @param B the second column type
     * @param C the third column type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @param block the processor
     * @return the processed result
     */
    inline fun <reified A : Any?, reified B : Any?, reified C : Any?, R> queryThreeColumns(
        template: CharSequence,
        condition: Any? = null,
        block: (Sequence<Triple<A, B, C>>) -> R
    ): R {
        val sql = dryRun.queryThreeColumns(template, condition)
        return `access$streamThreeColumns`<A, B, C>(sql, A::class, B::class, C::class).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Paginates entities by the SQL template.
     *
     * @param T the entity type
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @param limit the limit
     * @param offset the offset
     * @return the queried entities and the count of all entities
     */
    inline fun <reified T : Any> paginate(
        template: CharSequence,
        condition: Any? = null,
        limit: Int?,
        offset: Int?
    ): Pair<List<T>, Int> {
        require(T::class.isData) { "The T must be a data class." }
        val paginationTemplate = config.sqlRewriter.rewriteForPagination(template, limit, offset)
        val countTemplate = config.sqlRewriter.rewriteForCount(template)
        val list = query<T>(paginationTemplate, condition)
        val count = queryOneColumn<Int>(countTemplate, condition).first()
        return list to count
    }

    private fun streamMultiEntity(
        sql: Sql,
        desc: MultiEntityDesc
    ): Stream<List<Any>> = executeQuery(sql) { rs ->
        fromResultSetToStream(rs) {
            val row = mutableMapOf<PropDesc, Any?>()
            for ((i, propDesc) in desc.leafPropDescList.withIndex()) {
                val value = config.dialect.getValue(it, i + 1, propDesc.kClass)
                row[propDesc] = value
            }
            desc.new(row)
        }
    }

    private fun <T : Any> streamEntity(
        sql: Sql,
        desc: EntityDesc<T>
    ): Stream<T> = executeQuery(sql) { rs ->
        val propDescMap = mutableMapOf<Int, PropDesc>()
        val metaData = rs.metaData
        val count = metaData.columnCount
        for (i in 1..count) {
            val label = metaData.getColumnLabel(i).toLowerCase()
            val propDesc = desc.columnLabelMap[label] ?: continue
            propDescMap[i] = propDesc
        }
        fromResultSetToStream(rs) {
            val row = mutableMapOf<PropDesc, Any?>()
            for ((index, propDesc) in propDescMap) {
                val value = config.dialect.getValue(it, index, propDesc.kClass)
                row[propDesc] = value
            }
            desc.new(row)
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
            log(sql)
            val ps = con.prepareStatement(sql.text)
            try {
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

    /**
     * Inserts an entity.
     *
     * @param T the entity type
     * @param entity the entity
     * @param option the insert option
     * @return the inserted entity
     * @throws UniqueConstraintException if the unique constraint is violated
     */
    inline fun <reified T : Any> insert(entity: T, option: InsertOption = InsertOption()): T {
        require(T::class.isData) { "The T must be a data class." }
        val (sql, desc, newEntity) = dryRun.insert(entity, option) { sequenceName ->
            queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
        }
        return `access$executeUpdate`(sql, false) { count ->
            check(count == 1)
            newEntity.let { newEntity ->
                desc.listener?.postInsert(newEntity, desc) ?: newEntity
            }.let { newEntity ->
                config.listener.postInsert(newEntity, desc)
            }
        }
    }

    /**
     * Deletes an entity.
     *
     * @param T the entity type
     * @param entity the entity
     * @param option the delete option
     * @return the deleted entity
     * @throws OptimisticLockException if the optimistic lock is failed
     */
    inline fun <reified T : Any> delete(entity: T, option: DeleteOption = DeleteOption()): T {
        require(T::class.isData) { "The T must be a data class." }
        val (sql, desc, newEntity) = dryRun.delete(entity, option)
        return `access$executeUpdate`(sql, !option.ignoreVersion && desc.version != null) {
            newEntity.let { newEntity ->
                desc.listener?.postDelete(newEntity, desc) ?: newEntity
            }.let { newEntity ->
                config.listener.postDelete(newEntity, desc)
            }
        }
    }

    /**
     * Updates an entity.
     *
     * @param T the entity type
     * @param entity the entity
     * @param option the update option
     * @return the updated entity
     * @throws UniqueConstraintException if the unique constraint is violated
     * @throws OptimisticLockException if the optimistic lock is failed
     */
    inline fun <reified T : Any> update(entity: T, option: UpdateOption = UpdateOption()): T {
        require(T::class.isData) { "The T must be a data class." }
        val (sql, desc, newEntity) = dryRun.update(entity, option)
        return `access$executeUpdate`(sql, !option.ignoreVersion && desc.version != null) {
            newEntity.let { newEntity ->
                desc.listener?.postUpdate(newEntity, desc) ?: newEntity
            }.let { newEntity ->
                config.listener.postUpdate(newEntity, desc)
            }
        }
    }

    /**
     * Merges an entity.
     *
     * @param T the entity type
     * @param entity the entity
     * @param keys the keys which are used when the entity is merged
     * @param insertOption the insert option
     * @param updateOption the update option
     * @return the merged entity
     * @throws UniqueConstraintException if the unique constraint is violated
     * @throws OptimisticLockException if the optimistic lock is failed
     */
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
        val (sql, desc, newEntity) = dryRun.merge(
            entity = entity,
            keys = *keys,
            insertOption = insertOption,
            updateOption = updateOption,
            callNextValue = { sequenceName ->
                queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
            }
        )
        return `access$executeUpdate`(sql, !updateOption.ignoreVersion && desc.version != null) {
            newEntity.let { newEntity ->
                desc.listener?.postMerge(newEntity, desc) ?: newEntity
            }.let {
                config.listener.postMerge(newEntity, desc)
            }
        }
    }

    private fun <T> executeUpdate(sql: Sql, versionCheck: Boolean, block: (Int) -> T): T {
        val count = try {
            config.connection.use { con ->
                log(sql)
                con.prepareStatement(sql.text).use { ps ->
                    ps.setUp()
                    ps.bind(sql.values)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolation(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        if (versionCheck && count != 1) {
            throw OptimisticLockException()
        }
        return block(count)
    }

    /**
     * Submits a batch of insert commands.
     *
     * @param T the entity type
     * @param entities the entities
     * @param option the insert option
     * @return the inserted entities
     * @throws UniqueConstraintException if the unique constraint is violated
     */
    inline fun <reified T : Any> batchInsert(entities: List<T>, option: InsertOption = InsertOption()): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        if (entities.isEmpty()) return entities
        val (sqls, desc, newEntities) = dryRun.batchInsert(entities, option) { sequenceName ->
            queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
        }
        return `access$executeBatch`(sqls, false) { counts ->
            check(counts.all { it == 1 })
            newEntities.map {
                it.let { newEntity ->
                    desc.listener?.postInsert(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.postInsert(newEntity, desc)
                }
            }
        }
    }

    /**
     * Submits a batch of delete commands.
     *
     * @param T the entity type
     * @param entities the entities
     * @param option the delete option
     * @return the deleted entities
     * @throws OptimisticLockException if the optimistic lock is failed
     */
    inline fun <reified T : Any> batchDelete(entities: List<T>, option: DeleteOption = DeleteOption()): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        if (entities.isEmpty()) return entities
        val (sqls, desc, newEntities) = dryRun.batchDelete(entities, option)
        return `access$executeBatch`(sqls, !option.ignoreVersion && desc.version != null) {
            newEntities.map {
                it.let { newEntity ->
                    desc.listener?.postDelete(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.postDelete(newEntity, desc)
                }
            }
        }
    }

    /**
     * Submits a batch of update commands.
     *
     * @param T the entity type
     * @param entities the entities
     * @param option the update option
     * @return the updated entities
     * @throws UniqueConstraintException if the unique constraint is violated
     * @throws OptimisticLockException if the optimistic lock is failed
     */
    inline fun <reified T : Any> batchUpdate(entities: List<T>, option: UpdateOption = UpdateOption()): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        val (sqls, desc, newEntities) = dryRun.batchUpdate(entities, option)
        return `access$executeBatch`(sqls, !option.ignoreVersion && desc.version != null) {
            newEntities.map {
                it.let { newEntity ->
                    desc.listener?.postUpdate(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.postUpdate(newEntity, desc)
                }
            }
        }
    }

    /**
     * Submits a batch of merge commands.
     *
     * @param T the entity type
     * @param entities the entities
     * @param keys the keys which are used when the entity is merged
     * @param insertOption the insert option
     * @param updateOption the update option
     * @return the merged entities
     * @throws UniqueConstraintException if the unique constraint is violated
     * @throws OptimisticLockException if the optimistic lock is failed
     */
    inline fun <reified T : Any> batchMerge(
        entities: List<T>,
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
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        if (entities.isEmpty()) return entities
        val (sqls, desc, newEntities) = dryRun.batchMerge(
            entities = entities,
            keys = *keys,
            insertOption = insertOption,
            updateOption = updateOption,
            callNextValue = { sequenceName ->
                queryOneColumn<Long>(config.dialect.getSequenceSql(sequenceName)).first()
            }
        )
        return `access$executeBatch`(sqls, !updateOption.ignoreVersion && desc.version != null) {
            newEntities.map {
                it.let { newEntity ->
                    desc.listener?.postMerge(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.postMerge(newEntity, desc)
                }
            }
        }
    }

    private fun <T> executeBatch(sqls: Collection<Sql>, versionCheck: Boolean, block: (IntArray) -> T): T {
        val counts = try {
            config.connection.use { con ->
                val firstSql = sqls.first()
                log(firstSql)
                con.prepareStatement(firstSql.text).use { ps ->
                    val batchSize = config.batchSize
                    val allCounts = IntArray(sqls.size)
                    var offset = 0
                    for ((i, sql) in sqls.withIndex()) {
                        if (i > 0) {
                            log(sql)
                        }
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
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolation(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
        if (versionCheck && counts.any { it != 1 }) {
            throw OptimisticLockException()
        }
        return block(counts)
    }

    /**
     * Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement.
     *
     * @param template the SQL template
     * @param condition the parameter object for the SQL template
     * @return the row count for SQL Data Manipulation Language (DML) statements
     */
    fun executeUpdate(template: CharSequence, condition: Any? = null): Int {
        val ctx = config.objectDescFactory.toMap(condition)
        val sql = config.sqlBuilder.build(template, ctx)
        return executeUpdate(sql, false) { it }
    }

    /**
     * Executes the given SQL statements that returns nothing, such as SQL DDL statements.
     *
     * @param statements the SQL statements
     */
    fun execute(statements: CharSequence) {
        val sql = Sql(statements.toString(), emptyList(), null)
        executeUpdate(sql, false) {}
    }

    /**
     * Creates Array objects.
     *
     * @param typeName the SQL name of the type the elements of the array map to
     * @param elements the elements that populate the returned object
     */
    fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array = config.connection.use {
        it.createArrayOf(typeName, elements.toTypedArray())
    }

    /**
     * Creates a Blob object.
     */
    fun createBlob(): Blob = config.connection.use {
        it.createBlob()
    }

    /**
     * Creates a Clob object.
     */
    fun createClob(): Clob = config.connection.use {
        it.createClob()
    }

    /**
     * Creates a NClob object.
     */
    fun createNClob(): NClob = config.connection.use {
        it.createNClob()
    }

    /**
     * Creates a SQLXML object.
     */
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
        desc: MultiEntityDesc
    ) = streamMultiEntity(sql, desc)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T : Any> `access$streamEntity`(
        sql: Sql,
        desc: EntityDesc<T>
    ) = streamEntity(sql, desc)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T : Any?> `access$streamOneColumn`(
        sql: Sql,
        type: KClass<*>
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
    internal fun <T> `access$executeUpdate`(
        sql: Sql,
        versionCheck: Boolean,
        block: (Int) -> T
    ) = executeUpdate(sql, versionCheck, block)

    @PublishedApi
    @Suppress("UNUSED", "FunctionName")
    internal fun <T> `access$executeBatch`(
        sqls: Collection<Sql>,
        versionCheck: Boolean,
        block: (IntArray) -> T
    ) = executeBatch(sqls, versionCheck, block)

    /**
     * A dry run for database commands.
     *
     * @param config the database configuration
     */
    class DryRun(val config: DbConfig) {

        /**
         * Returns the result of a dry run for [Db.findById].
         *
         * @param T the entity type
         * @param id the identifier (primary key)
         * @param version the version value
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> findById(id: Any, version: Any? = null): Pair<Sql, EntityDesc<T>> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val sql = config.entitySqlBuilder.buildFindById(desc, id, version)
            return sql to desc
        }

        /**
         * Returns the result of a dry run for [Db.select].
         *
         * @param T the entity type
         * @param criteriaBlock the criteria
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> select(
            criteriaBlock: CriteriaScope<T>.() -> Unit = { }
        ): Pair<Sql, MultiEntityDesc> {
            require(T::class.isData) { "The T must be a data class." }
            val scope = CriteriaScope(T::class).also { it.criteriaBlock() }
            val processor = CriteriaProcessor(config.dialect, config.entityDescFactory, scope())
            val sql = processor.buildSelect()
            return sql to processor
        }

        /**
         * Returns the result of a dry run for [Db.query].
         *
         * @param T the entity type
         * @param template the SQL template
         * @param condition the parameter object for the SQL template
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> query(
            template: CharSequence,
            condition: Any? = null
        ): Pair<Sql, EntityDesc<T>> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val ctx = config.objectDescFactory.toMap(condition)
            val sql = config.sqlBuilder.build(template, ctx, desc.expander)
            return sql to desc
        }

        /**
         * Returns the result of a dry run for [Db.queryOneColumn].
         *
         * @param template the SQL template
         * @param condition the parameter object for the SQL template
         * @return the SQL
         */
        fun queryOneColumn(
            template: CharSequence,
            condition: Any? = null
        ): Sql {
            val ctx = config.objectDescFactory.toMap(condition)
            return config.sqlBuilder.build(template, ctx)
        }

        /**
         * Returns the result of a dry run for [Db.queryTwoColumns].
         *
         * @param template the SQL template
         * @param condition the parameter object for the SQL template
         * @return the SQL
         */
        fun queryTwoColumns(
            template: CharSequence,
            condition: Any? = null
        ): Sql {
            val ctx = config.objectDescFactory.toMap(condition)
            return config.sqlBuilder.build(template, ctx)
        }

        /**
         * Returns the result of a dry run for [Db.queryThreeColumns].
         *
         * @param template the SQL template
         * @param condition the parameter object for the SQL template
         * @return the SQL
         */
        fun queryThreeColumns(
            template: CharSequence,
            condition: Any? = null
        ): Sql {
            val ctx = config.objectDescFactory.toMap(condition)
            return config.sqlBuilder.build(template, ctx)
        }

        /**
         * Returns the result of a dry run for [Db.paginate].
         *
         * @param T the entity type
         * @param template the SQL template
         * @param condition the parameter object for the SQL template
         * @param limit the limit
         * @param offset the offset
         * @return the pagination SQL, the metadata and the count SQL
         */
        inline fun <reified T : Any> paginate(
            template: CharSequence,
            condition: Any? = null,
            limit: Int?,
            offset: Int?
        ): Triple<Sql, EntityDesc<T>, Sql> {
            require(T::class.isData) { "The T must be a data class." }
            val paginationTemplate = config.sqlRewriter.rewriteForPagination(template, limit, offset)
            val countTemplate = config.sqlRewriter.rewriteForCount(template)
            val (sql, desc) = query<T>(paginationTemplate, condition)
            val countSql = queryOneColumn(countTemplate, condition)
            return Triple(sql, desc, countSql)
        }

        /**
         * Returns the result of a dry run for [Db.insert].
         *
         * @param T the entity type
         * @param entity the entity
         * @param option the insert option
         * @param callNextValue the id generator
         * @return the SQL, the metadata and the new entity
         */
        inline fun <reified T : Any> insert(
            entity: T,
            option: InsertOption = InsertOption(),
            noinline callNextValue: (String) -> Long = { 0L }
        ): Triple<Sql, EntityDesc<T>, T> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            return if (option.assignId) {
                desc.assignId(entity, config.name, callNextValue)
            } else {
                entity
            }.let { newEntity ->
                if (option.assignTimestamp) desc.assignTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                desc.listener?.preInsert(newEntity, desc) ?: newEntity
            }.let { newEntity ->
                config.listener.preInsert(newEntity, desc)
            }.let { newEntity ->
                val sql = config.entitySqlBuilder.buildInsert(desc, newEntity, option)
                Triple(sql, desc, newEntity)
            }
        }

        /**
         * Returns the result of a dry run for [Db.delete].
         *
         * @param T the entity type
         * @param entity the entity
         * @param option the delete option
         * @return the SQL, the metadata and the new entity
         */
        inline fun <reified T : Any> delete(
            entity: T,
            option: DeleteOption = DeleteOption()
        ): Triple<Sql, EntityDesc<T>, T> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            return (desc.listener?.preDelete(entity, desc) ?: entity)
                .let { newEntity ->
                    config.listener.preDelete(newEntity, desc)
                }.let { newEntity ->
                    val sql = config.entitySqlBuilder.buildDelete(desc, newEntity, option)
                    Triple(sql, desc, newEntity)
                }
        }

        /**
         * Returns the result of a dry run for [Db.update].
         *
         * @param T the entity type
         * @param entity the entity
         * @param option the update option
         * @return the SQL, the metadata and the new entity
         */
        inline fun <reified T : Any> update(
            entity: T,
            option: UpdateOption = UpdateOption()
        ): Triple<Sql, EntityDesc<T>, T> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            return (if (option.incrementVersion) desc.incrementVersion(entity) else entity).let { newEntity ->
                if (option.updateTimestamp) desc.updateTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                desc.listener?.preUpdate(newEntity, desc) ?: newEntity
            }.let { newEntity ->
                config.listener.preUpdate(newEntity, desc)
            }.let { newEntity ->
                val sql = config.entitySqlBuilder.buildUpdate(desc, entity, newEntity, option)
                Triple(sql, desc, newEntity)
            }
        }

        /**
         * Returns the result of a dry run for [Db.merge].
         *
         * @param T the entity type
         * @param entity the entity
         * @param keys the keys which are used when the entity is merged
         * @param insertOption the insert option
         * @param updateOption the update option
         * @param callNextValue the id generator
         * @return the SQL, the metadata and the new entity
         */
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
            ),
            noinline callNextValue: (String) -> Long = { 0L }
        ): Triple<Sql, EntityDesc<T>, T> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            return if (insertOption.assignId) {
                desc.assignId(entity, config.name, callNextValue)
            } else {
                entity
            }.let { newEntity ->
                if (insertOption.assignTimestamp) desc.assignTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                if (updateOption.incrementVersion) desc.incrementVersion(newEntity) else newEntity
            }.let { newEntity ->
                if (updateOption.incrementVersion) desc.incrementVersion(newEntity) else newEntity
            }.let { newEntity ->
                if (updateOption.updateTimestamp) desc.updateTimestamp(newEntity) else newEntity
            }.let { newEntity ->
                desc.listener?.preMerge(newEntity, desc) ?: newEntity
            }.let { newEntity ->
                config.listener.preMerge(newEntity, desc)
            }.let { newEntity ->
                val buildMerge: (EntityDesc<T>, T, T, List<KProperty1<*, *>>, InsertOption, UpdateOption) -> Sql =
                    when {
                        config.dialect.supportsMerge() -> config.entitySqlBuilder::buildMerge
                        config.dialect.supportsUpsert() -> config.entitySqlBuilder::buildUpsert
                        else -> error("The merge command is not supported.")
                    }
                val sql = buildMerge(desc, entity, newEntity, keys.toList(), insertOption, updateOption)
                Triple(sql, desc, newEntity)
            }
        }

        /**
         * Returns the result of a dry run for [Db.batchInsert].
         *
         * @param T the entity type
         * @param entities the entities
         * @param option the insert option
         * @param callNextValue the id generator
         * @return the SQLs, the metadata and the new entities
         */
        inline fun <reified T : Any> batchInsert(
            entities: List<T>,
            option: InsertOption = InsertOption(),
            noinline callNextValue: (String) -> Long = { 0L }
        ): Triple<List<Sql>, EntityDesc<T>, List<T>> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val (sqls, newEntities) = entities.map { entity ->
                if (option.assignId) {
                    desc.assignId(entity, config.name, callNextValue)
                } else {
                    entity
                }.let { newEntity ->
                    if (option.assignTimestamp) desc.assignTimestamp(newEntity) else newEntity
                }.let { newEntity ->
                    desc.listener?.preInsert(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.preInsert(newEntity, desc)
                }.let { newEntity ->
                    val sql = config.entitySqlBuilder.buildInsert(desc, newEntity, option)
                    sql to newEntity
                }
            }.fold(entities.size.let { ArrayList<Sql>(it) to ArrayList<T>(it) }) { acc, (s, e) ->
                acc.also { it.first.add(s); it.second.add(e) }
            }
            return Triple(sqls, desc, newEntities)
        }

        /**
         * Returns the result of a dry run for [Db.batchDelete].
         *
         * @param T the entity type
         * @param entities the entities
         * @param option the delete option
         * @return the SQLs, the metadata and the new entities
         */
        inline fun <reified T : Any> batchDelete(
            entities: List<T>,
            option: DeleteOption = DeleteOption()
        ): Triple<List<Sql>, EntityDesc<T>, List<T>> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val (sqls, newEntities) = entities.map { entity ->
                (desc.listener?.preDelete(entity, desc) ?: entity)
                    .let { newEntity ->
                        config.listener.preDelete(newEntity, desc)
                    }.let { newEntity ->
                        val sql = config.entitySqlBuilder.buildDelete(desc, newEntity, option)
                        sql to newEntity
                    }
            }.fold(entities.size.let { ArrayList<Sql>(it) to ArrayList<T>(it) }) { acc, (s, e) ->
                acc.also { it.first.add(s); it.second.add(e) }
            }
            return Triple(sqls, desc, newEntities)
        }

        /**
         * Returns the result of a dry run for [Db.batchUpdate].
         *
         * @param T the entity type
         * @param entities the entities
         * @param option the update option
         * @return the SQLs, the metadata and the new entities
         */
        inline fun <reified T : Any> batchUpdate(
            entities: List<T>,
            option: UpdateOption = UpdateOption()
        ): Triple<List<Sql>, EntityDesc<T>, List<T>> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val (sqls, newEntities) = entities.map { entity ->
                (if (option.incrementVersion) desc.incrementVersion(entity) else entity).let { newEntity ->
                    if (option.updateTimestamp) desc.updateTimestamp(newEntity) else newEntity
                }.let { newEntity ->
                    desc.listener?.preUpdate(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.preUpdate(newEntity, desc)
                }.let { newEntity ->
                    val sql = config.entitySqlBuilder.buildUpdate(desc, entity, newEntity, option)
                    sql to newEntity
                }
            }.fold(entities.size.let { ArrayList<Sql>(it) to ArrayList<T>(it) }) { acc, (s, e) ->
                acc.also { it.first.add(s); it.second.add(e) }
            }
            return Triple(sqls, desc, newEntities)
        }

        /**
         * Returns the result of a dry run for [Db.batchMerge].
         *
         * @param T the entity type
         * @param entities the entities
         * @param keys the keys which are used when the entity is merged
         * @param insertOption the insert option
         * @param updateOption the update option
         * @param callNextValue the id generator
         * @return the SQLs, the metadata and the new entities
         */
        inline fun <reified T : Any> batchMerge(
            entities: List<T>,
            vararg keys: KProperty1<*, *>,
            insertOption: InsertOption = InsertOption(
                assignId = false,
                assignTimestamp = false
            ),
            updateOption: UpdateOption = UpdateOption(
                incrementVersion = false,
                updateTimestamp = false,
                ignoreVersion = true
            ),
            noinline callNextValue: (String) -> Long = { 0L }
        ): Triple<List<Sql>, EntityDesc<T>, List<T>> {
            require(T::class.isData) { "The T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val (sqls, newEntities) = entities.map { entity ->
                if (insertOption.assignId) {
                    desc.assignId(entity, config.name, callNextValue)
                } else {
                    entity
                }.let { newEntity ->
                    if (insertOption.assignTimestamp) desc.assignTimestamp(newEntity) else newEntity
                }.let { newEntity ->
                    if (updateOption.incrementVersion) desc.incrementVersion(newEntity) else newEntity
                }.let { newEntity ->
                    if (updateOption.incrementVersion) desc.incrementVersion(newEntity) else newEntity
                }.let { newEntity ->
                    if (updateOption.updateTimestamp) desc.updateTimestamp(newEntity) else newEntity
                }.let { newEntity ->
                    desc.listener?.preMerge(newEntity, desc) ?: newEntity
                }.let { newEntity ->
                    config.listener.preMerge(newEntity, desc)
                }.let { newEntity ->
                    val buildMerge: (EntityDesc<T>, T, T, List<KProperty1<*, *>>, InsertOption, UpdateOption) -> Sql =
                        when {
                            config.dialect.supportsMerge() -> config.entitySqlBuilder::buildMerge
                            config.dialect.supportsUpsert() -> config.entitySqlBuilder::buildUpsert
                            else -> error("The merge command is not supported.")
                        }
                    val sql = buildMerge(desc, entity, newEntity, keys.toList(), insertOption, updateOption)
                    sql to newEntity
                }
            }.fold(entities.size.let { ArrayList<Sql>(it) to ArrayList<T>(it) }) { acc, (s, e) ->
                acc.also { it.first.add(s); it.second.add(e) }
            }
            return Triple(sqls, desc, newEntities)
        }
    }
}
