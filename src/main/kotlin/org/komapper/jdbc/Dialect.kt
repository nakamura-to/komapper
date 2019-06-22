package org.komapper.jdbc

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface Dialect {

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?

    fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)

    fun formatValue(value: Any?, valueClass: KClass<*>): String

    fun isUniqueConstraintViolation(exception: SQLException): Boolean

    fun getSequenceSql(sequenceName: String): String
}

abstract class AbstractDialect : Dialect {

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val jdbcType = getJdbcType(valueClass)
        return jdbcType.getValue(rs, index)
    }

    override fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        @Suppress("UNCHECKED_CAST")
        val jdbcType = getJdbcType(valueClass) as JdbcType<Any>
        jdbcType.setValue(stmt, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        @Suppress("UNCHECKED_CAST")
        val jdbcType = getJdbcType(valueClass) as JdbcType<Any>
        return jdbcType.toString(value)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun getJdbcType(type: KClass<*>): JdbcType<*> = when {
        type == Any::class -> AnyType
        type == java.sql.Array::class -> ArrayType
        type == BigDecimal::class -> BigDecimalType
        type == BigInteger::class -> BigIntegerType
        type == Blob::class -> BlobType
        type == Boolean::class -> BooleanType
        type == Byte::class -> ByteType
        type == ByteArray::class -> ByteArrayType
        type == Double::class -> DoubleType
        type == Clob::class -> ClobType
        type.isSubclassOf(Enum::class) -> EnumType(type as KClass<Enum<*>>)
        type == Float::class -> FloatType
        type == Int::class -> IntType
        type == LocalDateTime::class -> LocalDateTimeType
        type == LocalDate::class -> LocalDateType
        type == LocalTime::class -> LocalTimeType
        type == Long::class -> LongType
        type == NClob::class -> NClobType
        type == Short::class -> ShortType
        type == String::class -> StringType
        type == SQLXML::class -> SQLXMLType
        else -> error(
            "The jdbcType is not found for the type \"${type.qualifiedName}\"." +
                    "Are you forgetting to specify @Embedded to the property?"
        )
    }

    protected fun getCause(exception: SQLException): SQLException =
        exception.filterIsInstance(SQLException::class.java).first()
}

open class H2Dialect : AbstractDialect() {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }
}

open class PostgreSqlDialect : AbstractDialect() {

    companion object {
        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.sqlState == UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select nextval('$sequenceName')"
    }
}
