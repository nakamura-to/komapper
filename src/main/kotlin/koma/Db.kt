package koma

import koma.meta.ObjectMeta
import koma.meta.makeEntityMeta
import koma.sql.Sql
import koma.sql.SqlBuilder
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

class DbConfig {
    lateinit var dataSource: DataSource
    val dialect = Dialect()
}

class Dialect {
    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        return rs.getObject(index)
    }

    fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        stmt.setObject(index, value)
    }
}

open class Db(config: DbConfig) {
    protected val dataSource = config.dataSource
    protected val dialect = config.dialect

    inline fun <reified T : Any> select(
        template: CharSequence,
        condition: Any = object {}
    ): List<T> {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        return selectAsSequence(template, condition, T::class).toList()
    }

    inline fun <reified T : Any> select(
        template: CharSequence,
        condition: Any = object {},
        action: (T) -> Unit
    ) {
        require(T::class.isData) { "The T must a be data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        selectAsSequence(template, condition, T::class).forEach(action)
    }

    fun <T : Any> selectAsSequence(
        template: CharSequence,
        condition: Any = object {},
        clazz: KClass<T>
    ): Sequence<T> {
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
            sequence {
                while (rs.next()) {
                    val row = mutableMapOf<KParameter, Any?>()
                    for ((index, param) in paramMap) {
                        val value = dialect.getValue(rs, index, param.type.jvmErasure)
                        row[param] = value
                    }
                    val entity = meta.new(row)
                    yield(entity)
                }
            }
        }
    }

    inline fun <reified T : Any?> selectOneColumn(
        template: CharSequence,
        condition: Any = object {}
    ): List<T> {
        return selectOneColumnAsSequence<T>(template, condition, T::class).toList()
    }

    inline fun <reified T : Any?> selectOneColumn(
        template: CharSequence,
        condition: Any = object {},
        action: (T) -> Unit
    ) {
        selectOneColumnAsSequence<T>(template, condition, T::class).forEach(action)
    }


    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> selectOneColumnAsSequence(
        template: CharSequence,
        condition: Any = object {},
        type: KClass<*>
    ): Sequence<T> {
        return executeQuery(template.toString(), condition) { rs ->
            sequence {
                while (rs.next()) {
                    val value = dialect.getValue(rs, 1, type) as T
                    yield(value)
                }
            }
        }
    }

    protected fun <R : Any?> executeQuery(
        template: String,
        condition: Any,
        handler: (rs: ResultSet) -> Sequence<R>
    ): Sequence<R> {
        val objectMeta = ObjectMeta(condition::class)
        val ctx = objectMeta.toMap(condition)
        val sql = SqlBuilder().build(template, ctx)
        return execute(sql) { stmt ->
            sequence {
                stmt.executeQuery().use { rs ->
                    yieldAll(handler(rs))
                }
            }
        }
    }

    inline fun <reified T : Any> insert(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = makeEntityMeta(T::class)
        return meta.assignId(entity).also { newEntity ->
            val sql = meta.buildInsertSql(newEntity)
            val count = modify(sql)
            if (count == 0) TODO()
        }
    }

    inline fun <reified T : Any> delete(entity: T) {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = makeEntityMeta(T::class)
        val sql = meta.buildDeleteSql(entity)
        val count = modify(sql)
        if (count == 0) TODO()
    }

    inline fun <reified T : Any> update(entity: T): T {
        require(T::class.isData) { "The T must be a data class." }
        require(!T::class.isAbstract) { "The T must not be abstract." }
        val meta = makeEntityMeta(T::class)
        return meta.incrementVersion(entity).also { newEntity ->
            val sql = meta.buildUpdateSql(entity, newEntity)
            val count = modify(sql)
            if (count == 0) TODO()
        }
    }


    fun modify(sql: Sql): Int {
        // invoke toList() to close resources
        return executeUpdate(sql).toList().first()
    }


    protected fun executeUpdate(sql: Sql): Sequence<Int> {
        return execute(sql) { stmt ->
            sequence {
                yield(stmt.executeUpdate())
            }
        }
    }

    protected fun <T> execute(sql: Sql, handler: (PreparedStatement) -> Sequence<T>): Sequence<T> {
        return sequence {
            dataSource.connection.use { con ->
                con.prepareStatement(sql.text).use { stmt ->
                    bindValues(stmt, sql.values)
                    yieldAll(handler(stmt))
                }
            }
        }
    }

    protected fun bindValues(stmt: PreparedStatement, values: List<Pair<Any?, KClass<*>>>) {
        values.forEachIndexed { index, (value, valueType) ->
            dialect.setValue(stmt, index + 1, value, valueType)
        }
    }

}