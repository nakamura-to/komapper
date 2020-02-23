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
import org.komapper.core.builder.AggregationContext
import org.komapper.core.builder.AggregationDesc
import org.komapper.core.builder.DeleteBuilder
import org.komapper.core.builder.EntityData
import org.komapper.core.builder.EntityKey
import org.komapper.core.builder.InsertBuilder
import org.komapper.core.builder.SelectBuilder
import org.komapper.core.builder.UpdateBuilder
import org.komapper.core.criteria.Alias
import org.komapper.core.criteria.Delete
import org.komapper.core.criteria.DeleteCriteria
import org.komapper.core.criteria.DeleteScope
import org.komapper.core.criteria.Insert
import org.komapper.core.criteria.InsertCriteria
import org.komapper.core.criteria.InsertScope
import org.komapper.core.criteria.Select
import org.komapper.core.criteria.SelectCriteria
import org.komapper.core.criteria.SelectScope
import org.komapper.core.criteria.Update
import org.komapper.core.criteria.UpdateCriteria
import org.komapper.core.criteria.UpdateScope
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.PropDesc
import org.komapper.core.sql.Sql
import org.komapper.core.sql.Template
import org.komapper.core.sql.template
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
     * A transaction scope initiator.
     */
    val transaction by lazy { config.transactionScopeInitiator }

    /**
     * Finds an entity by Id and version.
     *
     * @param T the entity type
     * @param id the identifier (primary key)
     * @param version the version value
     * @return the found entity
     */
    inline fun <reified T : Any> findById(id: Any, version: Any? = null): T? {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, desc) = dryRun.findById<T>(id, version)
        return `access$streamEntity`(sql, desc).use { stream ->
            stream.toList().firstOrNull()
        }
    }

    /**
     * Selects entities by criteria.
     *
     * @param T the entity type
     * @param query the criteria query
     * @return the selected entities
     */
    inline fun <reified T : Any> select(
        query: Select<T> = { }
    ): List<T> {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, desc) = dryRun.select(query)
        return `access$processAggregation`(sql, desc)
    }

    /**
     * Selects entities by the SQL template.
     *
     * @param T the entity type
     * @param template the SQL template
     * @return the queried entities
     */
    inline fun <reified T : Any> select(
        template: Template<T>
    ): List<T> {
        require(T::class.isData) { "The type parameter T must be a data class." }
        return select(template, Sequence<T>::toList)
    }

    /**
     * Selects entities by the SQL template and process them as sequence.
     *
     * @param T the entity type
     * @param template the SQL template
     * @param block the result processor
     * @return the processed result
     */
    inline fun <reified T : Any, R> select(
        template: Template<T>,
        block: (Sequence<T>) -> R
    ): R {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, desc) = dryRun.select(template)
        return `access$streamEntity`(sql, desc).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Selects one column by the SQL template.
     *
     * @param T the column type
     * @param template the SQL template
     * @return the queried column values
     */
    inline fun <reified T : Any?> selectOneColumn(
        template: Template<T>
    ): List<T> = selectOneColumn(template, Sequence<T>::toList)

    /**
     * Selects one column by the SQL template and process them as sequence.
     *
     * @param T the column type
     * @param template the SQL template
     * @param block the result processor
     * @return the processed result
     */
    inline fun <reified T : Any?, R> selectOneColumn(
        template: Template<T>,
        block: (Sequence<T>) -> R
    ): R {
        val sql = dryRun.selectOneColumn(template)
        return `access$streamOneColumn`<T>(sql, T::class).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Selects two columns by the SQL template.
     *
     * @param A the first column type
     * @param B the second column type
     * @param template the SQL template
     * @return the queried column values
     */
    inline fun <reified A : Any?, reified B : Any?> selectTwoColumns(
        template: Template<Pair<A, B>>
    ): List<Pair<A, B>> = selectTwoColumns(template, Sequence<Pair<A, B>>::toList)

    /**
     * Selects two columns by the SQL template and process them as sequence.
     *
     * @param A the first column type
     * @param B the second column type
     * @param template the SQL template
     * @param block the result processor
     * @return the processed result
     */
    inline fun <reified A : Any?, reified B : Any?, R> selectTwoColumns(
        template: Template<Pair<A, B>>,
        block: (Sequence<Pair<A, B>>) -> R
    ): R {
        val sql = dryRun.selectTwoColumns(template)
        return `access$streamTwoColumns`<A, B>(sql, A::class, B::class).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Selects three columns by the SQL template.
     *
     * @param A the first column type
     * @param B the second column type
     * @param C the third column type
     * @param template the SQL template
     * @return the queried column values
     */
    inline fun <reified A : Any?, reified B : Any?, reified C : Any?> selectThreeColumns(
        template: Template<Triple<A, B, C>>
    ): List<Triple<A, B, C>> = selectThreeColumns(template, Sequence<Triple<A, B, C>>::toList)

    /**
     * Selects three columns by the SQL template and process them as sequence.
     *
     * @param A the first column type
     * @param B the second column type
     * @param C the third column type
     * @param template the SQL template
     * @param block the result processor
     * @return the processed result
     */
    inline fun <reified A : Any?, reified B : Any?, reified C : Any?, R> selectThreeColumns(
        template: Template<Triple<A, B, C>>,
        block: (Sequence<Triple<A, B, C>>) -> R
    ): R {
        val sql = dryRun.selectThreeColumns(template)
        return `access$streamThreeColumns`<A, B, C>(sql, A::class, B::class, C::class).use { stream ->
            block(stream.asSequence())
        }
    }

    /**
     * Paginates entities by the SQL template.
     *
     * @param T the entity type
     * @param template the SQL template
     * @param limit the limit
     * @param offset the offset
     * @return the queried entities and the count of all entities
     */
    inline fun <reified T : Any> paginate(
        template: Template<T>,
        limit: Int?,
        offset: Int?
    ): Pair<List<T>, Int> {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val paginationTemplate = config.sqlRewriter.rewriteForPagination(template, limit, offset)
        val countTemplate = config.sqlRewriter.rewriteForCount(template)
        val list = select(paginationTemplate)
        val count = selectOneColumn(countTemplate).first()
        return list to count
    }

    private fun <T : Any> processAggregation(
        sql: Sql,
        desc: AggregationDesc
    ): List<T> {
        val context = AggregationContext()
        val stream = executeQuery(sql) { rs ->
            while (rs.next()) {
                val row = mutableListOf<Pair<Alias, EntityData>>()
                var propIndex = 0
                for ((alias, entityDesc) in desc.fetchedEntityDescMap) {
                    val properties = mutableMapOf<PropDesc, Any?>()
                    for (propDesc in entityDesc.leafPropDescList) {
                        val value = config.dialect.getValue(rs, propIndex + 1, propDesc.kClass)
                        properties[propDesc] = value
                        propIndex++
                    }
                    val keyAndData = context[alias]
                    val key = EntityKey(entityDesc, properties)
                    val data = keyAndData.getOrPut(key) { EntityData(key) }
                    row.forEach { (a, d) ->
                        d.associate(alias, data)
                        data.associate(a, d)
                    }
                    row.add(alias to data)
                }
            }
            Stream.empty<T>()
        }
        // release resources immediately
        stream.close()
        @Suppress("UNCHECKED_CAST")
        return desc.process(context) as List<T>
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
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, desc, newEntity) = dryRun.insert(entity, option) { sequenceName ->
            val t = template<Long>(config.dialect.getSequenceSql(sequenceName))
            selectOneColumn(t).first()
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
     * Inserts by criteria.
     *
     * @param query the criteria query
     * @return the affected row count
     */
    inline fun <reified T : Any> insert(query: Insert<T>): Int {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, _) = dryRun.insert(query)
        return `access$executeUpdate`(sql, false) { it }
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
        require(T::class.isData) { "The type parameter T must be a data class." }
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
     * Deletes by criteria.
     *
     * @param query the criteria query
     * @return the affected row count
     */
    inline fun <reified T : Any> delete(query: Delete<T>): Int {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, _) = dryRun.delete(query)
        return `access$executeUpdate`(sql, false) { it }
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
        require(T::class.isData) { "The type parameter T must be a data class." }
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
     * Updates by criteria.
     *
     * @param query the criteria query
     * @return the affected row count
     */
    inline fun <reified T : Any> update(query: Update<T>): Int {
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, _) = dryRun.update(query)
        return `access$executeUpdate`(sql, false) { it }
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
        require(T::class.isData) { "The type parameter T must be a data class." }
        val (sql, desc, newEntity) = dryRun.merge(
            entity = entity,
            keys = *keys,
            insertOption = insertOption,
            updateOption = updateOption,
            callNextValue = { sequenceName ->
                val t = template<Long>(config.dialect.getSequenceSql(sequenceName))
                selectOneColumn(t).first()
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
        require(T::class.isData) { "The type parameter T must be a data class." }
        if (entities.isEmpty()) return entities
        val (sqls, desc, newEntities) = dryRun.batchInsert(entities, option) { sequenceName ->
            val t = template<Long>(config.dialect.getSequenceSql(sequenceName))
            selectOneColumn(t).first()
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
        require(T::class.isData) { "The type parameter T must be a data class." }
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
        require(T::class.isData) { "The type parameter T must be a data class." }
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
        require(T::class.isData) { "The type parameter T must be a data class." }
        if (entities.isEmpty()) return entities
        val (sqls, desc, newEntities) = dryRun.batchMerge(
            entities = entities,
            keys = *keys,
            insertOption = insertOption,
            updateOption = updateOption,
            callNextValue = { sequenceName ->
                val t = template<Long>(config.dialect.getSequenceSql(sequenceName))
                selectOneColumn(t).first()
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
     * @return the row count for SQL Data Manipulation Language (DML) statements
     */
    fun executeUpdate(template: Template<Int>): Int {
        val ctx = config.objectDescFactory.toMap(template.args)
        val sql = config.sqlBuilder.build(template.sql, ctx)
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
    internal fun <T : Any> `access$processAggregation`(
        sql: Sql,
        desc: AggregationDesc
    ) = processAggregation<T>(sql, desc)

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
            require(T::class.isData) { "The type parameter T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val sql = config.entitySqlBuilder.buildFindById(desc, id, version)
            return sql to desc
        }

        /**
         * Returns the result of a dry run for [Db.select].
         *
         * @param T the entity type
         * @param query the criteria query
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> select(
            query: Select<T> = { }
        ): Pair<Sql, AggregationDesc> {
            require(T::class.isData) { "The type parameter T must be a data class." }
            val criteria = SelectCriteria(T::class).also {
                SelectScope(it).query(it.alias)
            }
            val builder = SelectBuilder(
                config.dialect,
                config.entityDescFactory,
                criteria
            )
            val sql = builder.build()
            return sql to builder
        }

        /**
         * Returns the result of a dry run for [Db.select].
         *
         * @param T the entity type
         * @param template the SQL template
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> select(
            template: Template<T>
        ): Pair<Sql, EntityDesc<T>> {
            require(T::class.isData) { "The type parameter T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val ctx = config.objectDescFactory.toMap(template.args)
            val sql = config.sqlBuilder.build(template.sql, ctx, desc.expander)
            return sql to desc
        }

        /**
         * Returns the result of a dry run for [Db.selectOneColumn].
         *
         * @param template the SQL template
         * @return the SQL
         */
        fun <T : Any?> selectOneColumn(
            template: Template<T>
        ): Sql {
            val ctx = config.objectDescFactory.toMap(template.args)
            return config.sqlBuilder.build(template.sql, ctx)
        }

        /**
         * Returns the result of a dry run for [Db.selectTwoColumns].
         *
         * @param template the SQL template
         * @return the SQL
         */
        fun <A, B> selectTwoColumns(
            template: Template<Pair<A, B>>
        ): Sql {
            val ctx = config.objectDescFactory.toMap(template.args)
            return config.sqlBuilder.build(template.sql, ctx)
        }

        /**
         * Returns the result of a dry run for [Db.selectThreeColumns].
         *
         * @param template the SQL template
         * @return the SQL
         */
        fun <A, B, C> selectThreeColumns(
            template: Template<Triple<A, B, C>>
        ): Sql {
            val ctx = config.objectDescFactory.toMap(template.args)
            return config.sqlBuilder.build(template.sql, ctx)
        }

        /**
         * Returns the result of a dry run for [Db.paginate].
         *
         * @param T the entity type
         * @param template the SQL template
         * @param limit the limit
         * @param offset the offset
         * @return the pagination SQL, the metadata and the count SQL
         */
        inline fun <reified T : Any> paginate(
            template: Template<T>,
            limit: Int?,
            offset: Int?
        ): Triple<Sql, EntityDesc<T>, Sql> {
            require(T::class.isData) { "The type parameter T must be a data class." }
            val paginationTemplate = config.sqlRewriter.rewriteForPagination(template, limit, offset)
            val countTemplate = config.sqlRewriter.rewriteForCount(template)
            val (sql, desc) = select(paginationTemplate)
            val countSql = selectOneColumn(countTemplate)
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
         * @param query the criteria query
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> insert(
            query: Insert<T>
        ): Pair<Sql, EntityDesc<T>> {
            require(T::class.isData) { "The type parameter T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val criteria = InsertCriteria(T::class).also {
                InsertScope(it).query(it.alias)
            }
            val builder = InsertBuilder(
                config.dialect,
                config.entityDescFactory,
                criteria
            )
            val sql = builder.build()
            return sql to desc
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
         * Returns the result of a dry run for [Db.delete].
         *
         * @param query the criteria query
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> delete(
            query: Delete<T>
        ): Pair<Sql, EntityDesc<T>> {
            require(T::class.isData) { "The type parameter T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val criteria = DeleteCriteria(T::class).also {
                DeleteScope(it).query(it.alias)
            }
            val builder = DeleteBuilder(
                config.dialect,
                config.entityDescFactory,
                criteria
            )
            val sql = builder.build()
            return sql to desc
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
         * Returns the result of a dry run for [Db.update].
         *
         * @param query the criteria query
         * @return the SQL and the metadata
         */
        inline fun <reified T : Any> update(
            query: Update<T>
        ): Pair<Sql, EntityDesc<T>> {
            require(T::class.isData) { "The type parameter T must be a data class." }
            val desc = config.entityDescFactory.get(T::class)
            val criteria = UpdateCriteria(T::class).also {
                UpdateScope(it).query(it.alias)
            }
            val builder = UpdateBuilder(
                config.dialect,
                config.entityDescFactory,
                criteria
            )
            val sql = builder.build()
            return sql to desc
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
            require(T::class.isData) { "The type parameter T must be a data class." }
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
