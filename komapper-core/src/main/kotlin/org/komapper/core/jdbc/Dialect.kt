package org.komapper.core.jdbc

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface Dialect {
    val openQuote: String
    val closeQuote: String
    val escapePattern: Pattern
    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?
    fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)
    fun formatValue(value: Any?, valueClass: KClass<*>): String
    fun isUniqueConstraintViolation(exception: SQLException): Boolean
    fun getSequenceSql(sequenceName: String): String
    fun quote(name: String): String
    fun supportsMerge(): Boolean
    fun supportsUpsert(): Boolean
    fun escape(text: CharSequence): CharSequence
}

abstract class AbstractDialect : Dialect {

    override val openQuote: String = "\""
    override val closeQuote: String = "\""
    override val escapePattern: Pattern = Pattern.compile("""[\\_%]""")

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        @Suppress("UNCHECKED_CAST")
        val dataType = getDataType(valueClass) as DataType<Any>
        dataType.setValue(stmt, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        @Suppress("UNCHECKED_CAST")
        val dataType = getDataType(valueClass) as DataType<Any>
        return dataType.toString(value)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun getDataType(type: KClass<*>): DataType<*> = when {
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
        type == OffsetDateTime::class -> OffsetDateTimeType
        type == Short::class -> ShortType
        type == String::class -> StringType
        type == SQLXML::class -> SQLXMLType
        else -> error(
            "The dataType is not found for the type \"${type.qualifiedName}\"." +
                    "Are you forgetting to specify @Embedded to the property?"
        )
    }

    protected fun getCause(exception: SQLException): SQLException =
        exception.filterIsInstance(SQLException::class.java).first()

    override fun quote(name: String): String =
        name.split('.').joinToString(".") { openQuote + it + closeQuote }

    override fun supportsMerge(): Boolean = false

    override fun supportsUpsert(): Boolean = false

    override fun escape(text: CharSequence): CharSequence {
        val matcher = escapePattern.matcher(text)
        return matcher.replaceAll("""\\$0""")
    }
}

open class H2Dialect(val version: Version = Version.V1_4) : AbstractDialect() {

    companion object {
        enum class Version { V1_4 }

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

    override fun supportsMerge(): Boolean = true

    override fun supportsUpsert(): Boolean = false
}

open class PostgreSqlDialect(val version: Version = Version.V10) : AbstractDialect() {

    companion object {
        enum class Version { V10 }

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

    override fun supportsMerge(): Boolean = false

    override fun supportsUpsert(): Boolean = true
}
