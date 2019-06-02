package org.komapper

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

    fun isUniqueConstraintViolated(exception: SQLException): Boolean

    fun getSequenceSql(sequenceName: String): String
}

abstract class AbstractDialect : Dialect {

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val jdbcType = getJdbcType(valueClass)
        return jdbcType.getValue(rs, index)
    }

    override fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        @Suppress("UNCHECKED_CAST")
        val jdbcType = getJdbcType(valueClass) as org.komapper.jdbc.JdbcType<Any>
        jdbcType.setValue(stmt, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        @Suppress("UNCHECKED_CAST")
        val jdbcType = getJdbcType(valueClass) as org.komapper.jdbc.JdbcType<Any>
        return jdbcType.toString(value)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun getJdbcType(type: KClass<*>): org.komapper.jdbc.JdbcType<*> = when {
        type == Any::class -> org.komapper.jdbc.AnyType
        type == java.sql.Array::class -> org.komapper.jdbc.ArrayType
        type == BigDecimal::class -> org.komapper.jdbc.BigDecimalType
        type == BigInteger::class -> org.komapper.jdbc.BigIntegerType
        type == Blob::class -> org.komapper.jdbc.BlobType
        type == Boolean::class -> org.komapper.jdbc.BooleanType
        type == Byte::class -> org.komapper.jdbc.ByteType
        type == ByteArray::class -> org.komapper.jdbc.ByteArrayType
        type == Double::class -> org.komapper.jdbc.DoubleType
        type == Clob::class -> org.komapper.jdbc.ClobType
        type.isSubclassOf(Enum::class) -> org.komapper.jdbc.EnumType(type as KClass<Enum<*>>)
        type == Float::class -> org.komapper.jdbc.FloatType
        type == Int::class -> org.komapper.jdbc.IntType
        type == LocalDateTime::class -> org.komapper.jdbc.LocalDateTimeType
        type == LocalDate::class -> org.komapper.jdbc.LocalDateType
        type == LocalTime::class -> org.komapper.jdbc.LocalTimeType
        type == Long::class -> org.komapper.jdbc.LongType
        type == NClob::class -> org.komapper.jdbc.NClobType
        type == Short::class -> org.komapper.jdbc.ShortType
        type == String::class -> org.komapper.jdbc.StringType
        type == SQLXML::class -> org.komapper.jdbc.SQLXMLType
        else -> error("""The jdbcType not found for the type "${type.qualifiedName}".""")
    }

    protected fun getErrorCode(exception: SQLException): Int {
        val cause = getCauseSQLException(exception)
        return cause.errorCode
    }

    private fun getCauseSQLException(exception: SQLException): SQLException {
        return exception.filterIsInstance(SQLException::class.java).first()
    }
}

class H2Dialect : org.komapper.AbstractDialect() {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
    }

    override fun isUniqueConstraintViolated(exception: SQLException): Boolean {
        val code = getErrorCode(exception)
        return UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE == code
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }
}
