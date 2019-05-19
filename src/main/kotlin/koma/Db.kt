package koma

import koma.meta.ObjectMeta
import koma.meta.makeEntityMeta
import koma.sql.SqlBuilder
import koma.sql.createDeleteSql
import koma.sql.createUpdateSql
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
    fun getValue(resultSet: ResultSet, index: Int, valueKClass: KClass<*>): Any? {
        return resultSet.getObject(index)
    }

    fun setValue(statement: PreparedStatement, index: Int, value: Any?, valueKClass: KClass<*>) {
        statement.setObject(index, value)
    }
}

class Db(val config: DbConfig) {

    inline fun <reified T : Any?> select(
        sql: CharSequence,
        condition: Any = object {}
    ): List<T> {
        return selectAsSequence<T>(sql, condition).toList()
    }

    inline fun <reified T : Any?> select(
        sql: CharSequence,
        condition: Any = object {},
        action: (T) -> Unit
    ) {
        selectAsSequence<T>(sql, condition).forEach(action)
    }

    inline fun <reified T : Any?> selectAsSequence(
        sql: CharSequence,
        condition: Any = object {}
    ): Sequence<T> {
        val kClass = T::class
        if (kClass.isData) {
            val entityMeta = makeEntityMeta(kClass)
            return executeQuery(sql.toString(), condition) { resultSet ->
                val paramMap = mutableMapOf<Int, KParameter>()
                val metaData = resultSet.metaData
                val count = metaData.columnCount
                for (i in 1..count) {
                    val label = metaData.getColumnLabel(i).toLowerCase()
                    val kParameter = entityMeta.consParamMap[label] ?: continue
                    paramMap[i] = kParameter
                }
                sequence {
                    while (resultSet.next()) {
                        val row = mutableMapOf<KParameter, Any?>()
                        for (e in paramMap) {
                            val (index, kParameter) = e
                            val type = kParameter.type.jvmErasure
                            val value = config.dialect.getValue(resultSet, index, type)
                            row[kParameter] = value
                        }
                        val entity = entityMeta.new(row) as T
                        yield(entity)
                    }
                }
            }
        } else {
            return executeQuery(sql.toString(), condition) { resultSet ->
                sequence {
                    while (resultSet.next()) {
                        val value = config.dialect.getValue(resultSet, 1, kClass) as T
                        yield(value)
                    }
                }
            }

        }
    }

    fun <R : Any?> executeQuery(
        template: String,
        condition: Any,
        handler: (resultSet: ResultSet) -> Sequence<R>
    ): Sequence<R> {
        val objectMeta = ObjectMeta(condition::class)
        val ctx = objectMeta.toMap(condition)
        val sql = SqlBuilder().build(template, ctx)
        val connection = config.dataSource.connection
        return sequence {
            connection.use {
                connection.prepareStatement(sql.text).use { stmt ->
                    sql.values.forEachIndexed { index, (value, valueKClass) ->
                        config.dialect.setValue(stmt, index + 1, value, valueKClass)
                    }
                    stmt.executeQuery().use { resultSet ->
                        yieldAll(handler(resultSet))
                    }
                }
            }
        }
    }

    inline fun <reified T : Any> delete(entity: T) {
        val kClass = entity::class
        if (!kClass.isData) TODO()
        val entityMeta = makeEntityMeta(kClass)
        val sql = createDeleteSql(entity, entityMeta)
        val connection = config.dataSource.connection
        connection.use {
            connection.prepareStatement(sql.text).use { stmt ->
                sql.values.forEachIndexed { index, (value, valueKClass) ->
                    config.dialect.setValue(stmt, index + 1, value, valueKClass)
                }
                val count = stmt.executeUpdate()
                if (count == 0) TODO()
            }
        }
    }

    inline fun <reified T : Any> update(entity: T): T {
        val kClass = entity::class
        if (!kClass.isData) TODO()
        val entityMeta = makeEntityMeta(kClass)
        val (sql, version) = createUpdateSql(entity, entityMeta)
        val connection = config.dataSource.connection
        connection.use {
            connection.prepareStatement(sql.text).use { stmt ->
                sql.values.forEachIndexed { index, (value, valueKClass) ->
                    config.dialect.setValue(stmt, index + 1, value, valueKClass)
                }
                val count = stmt.executeUpdate()
                if (count == 0) TODO()
                if (version == null) {
                    return entity
                }
                val receiverArg = entityMeta.copy.parameters[0] to entity
                val versionArg = entityMeta.versionPropMeta!!.copyFunParam to version
                return entityMeta.copy(mapOf(receiverArg, versionArg)) as T
            }
        }
    }

}