package org.komapper.core.it

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.Id
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*

@ExtendWith(Env::class)
class DataTypeTest {

    @Suppress("unused")
    private enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    @Test
    fun bigDecimal(db: Db) {
        data class BigDecimalTest(@Id val id: Int, val value: BigDecimal)

        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.insert(data)
        val data2 = db.findById<BigDecimalTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger(db: Db) {
        data class BigIntegerTest(@Id val id: Int, val value: BigInteger)

        val data = BigIntegerTest(1, BigInteger.TEN)
        db.insert(data)
        val data2 = db.findById<BigIntegerTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun boolean(db: Db) {
        data class BooleanTest(@Id val id: Int, val value: Boolean)

        val data = BooleanTest(1, true)
        db.insert(data)
        val data2 = db.findById<BooleanTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun byte(db: Db) {
        data class ByteTest(@Id val id: Int, val value: Byte)

        val data = ByteTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ByteTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun byteArray(db: Db) {
        @Suppress("ArrayInDataClass")
        data class ByteArrayTest(@Id val id: Int, val value: ByteArray)

        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.insert(data)
        val data2 = db.findById<ByteArrayTest>(1)
        assertEquals(data.id, data2!!.id)
        assertArrayEquals(data.value, data2.value)
    }

    @Test
    fun double(db: Db) {
        data class DoubleTest(@Id val id: Int, val value: Double)

        val data = DoubleTest(1, 10.0)
        db.insert(data)
        val data2 = db.findById<DoubleTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun enum(db: Db) {
        data class EnumTest(@Id val id: Int, val value: Direction)

        val data = EnumTest(1, Direction.EAST)
        db.insert(data)
        val data2 = db.findById<EnumTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun float(db: Db) {
        data class FloatTest(@Id val id: Int, val value: Float)

        val data = FloatTest(1, 10.0f)
        db.insert(data)
        val data2 = db.findById<FloatTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun int(db: Db) {
        data class IntTest(@Id val id: Int, val value: Int)

        val data = IntTest(1, 10)
        db.insert(data)
        val data2 = db.findById<IntTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime(db: Db) {
        data class LocalDateTimeTest(@Id val id: Int, val value: LocalDateTime)

        val data = LocalDateTimeTest(1, LocalDateTime.of(2019, 6, 1, 12, 11, 10))
        db.insert(data)
        val data2 = db.findById<LocalDateTimeTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localDate(db: Db) {
        data class LocalDateTest(@Id val id: Int, val value: LocalDate)

        val data = LocalDateTest(1, LocalDate.of(2019, 6, 1))
        db.insert(data)
        val data2 = db.findById<LocalDateTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localTime(db: Db) {
        data class LocalTimeTest(@Id val id: Int, val value: LocalTime)

        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.insert(data)
        val data2 = db.findById<LocalTimeTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun long(db: Db) {
        data class LongTest(@Id val id: Int, val value: Long)

        val data = LongTest(1, 10L)
        db.insert(data)
        val data2 = db.findById<LongTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun offsetDateTime(db: Db) {
        data class OffsetDateTimeTest(@Id val id: Int, val value: OffsetDateTime)

        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.insert(data)
        val data2 = db.findById<OffsetDateTimeTest>(1)
        // all OffsetDateTime instances will have be in UTC (have offset 0)
        // https://jdbc.postgresql.org/documentation/head/java8-date-time.html
        assertEquals(OffsetDateTime.of(dateTime.minusHours(9), ZoneOffset.UTC), data2!!.value)
    }

    @Test
    fun short(db: Db) {
        data class ShortTest(@Id val id: Int, val value: Short)

        val data = ShortTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ShortTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun string(db: Db) {
        data class StringTest(@Id val id: Int, val value: String)

        val data = StringTest(1, "ABC")
        db.insert(data)
        val data2 = db.findById<StringTest>(1)
        assertEquals(data, data2)
    }
}
