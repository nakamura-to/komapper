package koma

import koma.jdbc.*
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
        val jdbcType = getJdbcType(valueClass) as JdbcType<Any>
        jdbcType.setValue(stmt, index, value)
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

class H2Dialect : AbstractDialect() {

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
