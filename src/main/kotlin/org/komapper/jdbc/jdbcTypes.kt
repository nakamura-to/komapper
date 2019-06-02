package org.komapper.jdbc

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass

interface JdbcType<T> {

    fun getValue(rs: ResultSet, index: Int): T?

    fun setValue(ps: PreparedStatement, index: Int, value: T?)

    fun toString(value: T?): String
}

abstract class AbstractJdbcType<T>(protected val sqlType: Int) : JdbcType<T> {

    override fun getValue(rs: ResultSet, index: Int): T? {
        val value = doGetValue(rs, index)
        return if (rs.wasNull()) null else value
    }

    protected abstract fun doGetValue(rs: ResultSet, index: Int): T?

    override fun setValue(ps: PreparedStatement, index: Int, value: T?) {
        if (value == null) {
            ps.setNull(index, sqlType)
        } else {
            doSetValue(ps, index, value)
        }
    }

    protected abstract fun doSetValue(ps: PreparedStatement, index: Int, value: T)

    override fun toString(value: T?): String {
        return if (value == null) "null" else doToString(value)
    }

    open fun doToString(value: T): String {
        return value.toString()
    }
}

object ArrayType : org.komapper.jdbc.AbstractJdbcType<java.sql.Array>(Types.ARRAY) {

    override fun doGetValue(rs: ResultSet, index: Int): java.sql.Array? {
        return rs.getArray(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: java.sql.Array) {
        ps.setArray(index, value)
    }
}

object BigDecimalType : org.komapper.jdbc.AbstractJdbcType<BigDecimal>(Types.DECIMAL) {

    override fun doGetValue(rs: ResultSet, index: Int): BigDecimal? {
        return rs.getBigDecimal(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: BigDecimal) {
        ps.setBigDecimal(index, value)
    }
}

object BigIntegerType : org.komapper.jdbc.JdbcType<BigInteger> {

    private val jdbcType = org.komapper.jdbc.BigDecimalType

    override fun getValue(rs: ResultSet, index: Int): BigInteger? {
        return org.komapper.jdbc.BigIntegerType.jdbcType.getValue(rs, index)?.toBigInteger()
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: BigInteger?) {
        org.komapper.jdbc.BigIntegerType.jdbcType.setValue(ps, index, value?.toBigDecimal())
    }

    override fun toString(value: BigInteger?): String {
        return org.komapper.jdbc.BigIntegerType.jdbcType.toString(value?.toBigDecimal())
    }
}

object BlobType : org.komapper.jdbc.AbstractJdbcType<Blob>(Types.BLOB) {

    override fun doGetValue(rs: ResultSet, index: Int): Blob? {
        return rs.getBlob(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Blob) {
        ps.setBlob(index, value)
    }
}

object BooleanType : org.komapper.jdbc.AbstractJdbcType<Boolean>(Types.BOOLEAN) {

    override fun doGetValue(rs: ResultSet, index: Int): Boolean? {
        return rs.getBoolean(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Boolean) {
        ps.setBoolean(index, value)
    }

    override fun doToString(value: Boolean): String {
        return "'$value'"
    }
}

object ByteType : org.komapper.jdbc.AbstractJdbcType<Byte>(Types.SMALLINT) {

    override fun doGetValue(rs: ResultSet, index: Int): Byte? {
        return rs.getByte(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Byte) {
        ps.setByte(index, value)
    }
}

object ByteArrayType : org.komapper.jdbc.AbstractJdbcType<ByteArray>(Types.BINARY) {

    override fun doGetValue(rs: ResultSet, index: Int): ByteArray? {
        return rs.getBytes(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: ByteArray) {
        ps.setBytes(index, value)
    }
}

object ClobType : org.komapper.jdbc.AbstractJdbcType<Clob>(Types.CLOB) {

    override fun doGetValue(rs: ResultSet, index: Int): Clob? {
        return rs.getClob(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Clob) {
        ps.setClob(index, value)
    }
}

object DoubleType : org.komapper.jdbc.AbstractJdbcType<Double>(Types.DOUBLE) {

    override fun doGetValue(rs: ResultSet, index: Int): Double? {
        return rs.getDouble(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Double) {
        ps.setDouble(index, value)
    }
}

private object DateType : org.komapper.jdbc.AbstractJdbcType<Date>(Types.DATE) {

    override fun doGetValue(rs: ResultSet, index: Int): Date? {
        return rs.getDate(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Date) {
        ps.setDate(index, value)
    }

    override fun doToString(value: Date): String {
        return "'$value'"
    }
}

class EnumType(private val kClass: KClass<Enum<*>>) : org.komapper.jdbc.JdbcType<Enum<*>> {

    private val jdbcType = org.komapper.jdbc.StringType

    override fun getValue(rs: ResultSet, index: Int): Enum<*>? {
        val value = jdbcType.getValue(rs, index) ?: return null
        return toEnumConstant(value)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Enum<*>?) {
        jdbcType.setValue(ps, index, value?.name)
    }

    override fun toString(value: Enum<*>?): String {
        return jdbcType.toString(value?.name)
    }

    fun toEnumConstant(value: String): Enum<*> {
        return kClass.java.enumConstants.first { it.name == value }
    }
}

object FloatType : org.komapper.jdbc.AbstractJdbcType<Float>(Types.FLOAT) {

    override fun doGetValue(rs: ResultSet, index: Int): Float? {
        return rs.getFloat(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Float) {
        ps.setFloat(index, value)
    }
}

object IntType : org.komapper.jdbc.AbstractJdbcType<Int>(Types.INTEGER) {

    override fun doGetValue(rs: ResultSet, index: Int): Int? {
        return rs.getInt(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Int) {
        ps.setInt(index, value)
    }
}

object LocalDateTimeType : org.komapper.jdbc.JdbcType<LocalDateTime> {

    private val jdbcType = org.komapper.jdbc.TimestampType

    override fun getValue(rs: ResultSet, index: Int): LocalDateTime? {
        return org.komapper.jdbc.LocalDateTimeType.jdbcType.getValue(rs, index)?.toLocalDateTime()
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: LocalDateTime?) {
        org.komapper.jdbc.LocalDateTimeType.jdbcType.setValue(ps, index, value?.toTimestamp())
    }

    override fun toString(value: LocalDateTime?): String {
        return org.komapper.jdbc.LocalDateTimeType.jdbcType.toString(value?.toTimestamp())
    }

    private fun LocalDateTime.toTimestamp(): Timestamp {
        return Timestamp.valueOf(this)
    }
}

object LocalDateType : org.komapper.jdbc.JdbcType<LocalDate> {

    private val jdbcType = org.komapper.jdbc.DateType

    override fun getValue(rs: ResultSet, index: Int): LocalDate? {
        return org.komapper.jdbc.LocalDateType.jdbcType.getValue(rs, index)?.toLocalDate()
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: LocalDate?) {
        org.komapper.jdbc.LocalDateType.jdbcType.setValue(ps, index, value?.toDate())
    }

    override fun toString(value: LocalDate?): String {
        return org.komapper.jdbc.LocalDateType.jdbcType.toString(value?.toDate())
    }

    private fun LocalDate.toDate(): Date {
        return Date.valueOf(this)
    }
}

object LocalTimeType : org.komapper.jdbc.JdbcType<LocalTime> {

    private val jdbcType = org.komapper.jdbc.TimeType

    override fun getValue(rs: ResultSet, index: Int): LocalTime? {
        return org.komapper.jdbc.LocalTimeType.jdbcType.getValue(rs, index)?.toLocalTime()
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: LocalTime?) {
        org.komapper.jdbc.LocalTimeType.jdbcType.setValue(ps, index, value?.toTime())
    }

    override fun toString(value: LocalTime?): String {
        return org.komapper.jdbc.LocalTimeType.jdbcType.toString(value?.toTime())
    }

    private fun LocalTime.toTime(): Time {
        return Time.valueOf(this)
    }
}

object LongType : org.komapper.jdbc.AbstractJdbcType<Long>(Types.BIGINT) {

    override fun doGetValue(rs: ResultSet, index: Int): Long? {
        return rs.getLong(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Long) {
        ps.setLong(index, value)
    }
}

object NClobType : org.komapper.jdbc.AbstractJdbcType<NClob>(Types.NCLOB) {

    override fun doGetValue(rs: ResultSet, index: Int): NClob? {
        return rs.getNClob(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: NClob) {
        ps.setNClob(index, value)
    }
}

object AnyType : org.komapper.jdbc.AbstractJdbcType<Any>(Types.OTHER) {

    override fun doGetValue(rs: ResultSet, index: Int): Any? {
        return rs.getObject(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Any) {
        ps.setObject(index, value, sqlType)
    }
}

object ShortType : org.komapper.jdbc.AbstractJdbcType<Short>(Types.SMALLINT) {

    override fun doGetValue(rs: ResultSet, index: Int): Short? {
        return rs.getShort(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Short) {
        ps.setShort(index, value)
    }
}

object StringType : org.komapper.jdbc.AbstractJdbcType<String>(Types.VARCHAR) {

    override fun doGetValue(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: String) {
        ps.setString(index, value)
    }

    override fun doToString(value: String): String {
        return "'$value'"
    }
}

object SQLXMLType : org.komapper.jdbc.AbstractJdbcType<SQLXML>(Types.SQLXML) {

    override fun doGetValue(rs: ResultSet, index: Int): SQLXML? {
        return rs.getSQLXML(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: SQLXML) {
        ps.setSQLXML(index, value)
    }
}

private object TimeType : org.komapper.jdbc.AbstractJdbcType<Time>(Types.TIME) {

    override fun doGetValue(rs: ResultSet, index: Int): Time? {
        return rs.getTime(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Time) {
        ps.setTime(index, value)
    }

    override fun doToString(value: Time): String {
        return "'$value'"
    }
}

private object TimestampType : org.komapper.jdbc.AbstractJdbcType<Timestamp>(Types.TIMESTAMP) {

    override fun doGetValue(rs: ResultSet, index: Int): Timestamp? {
        return rs.getTimestamp(index)
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Timestamp) {
        ps.setTimestamp(index, value)
    }

    override fun doToString(value: Timestamp): String {
        return "'$value'"
    }
}
