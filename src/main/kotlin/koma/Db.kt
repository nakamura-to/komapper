package koma

import koma.meta.PropMeta
import koma.meta.makeEntityMeta
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
        condition: Map<String, Pair<*, KClass<*>>> = emptyMap()
    ): List<T> {
        return selectAsSequence<T>(sql, condition).toList()
    }

    inline fun <reified T : Any?> select(
        sql: CharSequence,
        condition: Map<String, Pair<*, KClass<*>>> = emptyMap(),
        action: (T) -> Unit
    ) {
        selectAsSequence<T>(sql, condition).forEach(action)
    }

    inline fun <reified T : Any?> selectAsSequence(
        sql: CharSequence,
        condition: Map<String, Pair<*, KClass<*>>> = emptyMap()
    ): Sequence<T> {
        val kClass = T::class
        if (kClass.isData) {
            val entityMeta = makeEntityMeta(kClass)
            return executeQuery(sql.toString(), condition) { resultSet ->
                val propMetaMap = mutableMapOf<Int, PropMeta>()
                val metaData = resultSet.metaData
                val count = metaData.columnCount
                for (i in 1..count) {
                    val label = metaData.getColumnLabel(i).toLowerCase()
                    val propMeta = entityMeta.propMetaMap[label] ?: continue
                    propMetaMap[i] = propMeta
                }
                sequence {
                    while (resultSet.next()) {
                        val row = mutableMapOf<KParameter, Any?>()
                        for (e in propMetaMap) {
                            val (index, propMeta) = e
                            val type = propMeta.kParameter.type.jvmErasure
                            val value = config.dialect.getValue(resultSet, index, type)
                            row[propMeta.kParameter] = value
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
        condition: Map<String, Pair<*, KClass<*>>>,
        handler: (resultSet: ResultSet) -> Sequence<R>
    ): Sequence<R> {
        val sql = SqlBuilder().build(template, condition)
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

}