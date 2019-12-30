package org.komapper.core.it

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.metadata.EntityMetadata

@ExtendWith(Env::class)
class DataTypeTest {

    @Suppress("unused")
    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    @Suppress("ArrayInDataClass")
    data class ByteArrayTest(val id: Int, val value: ByteArray)
    object ByteArrayTestMetadata : EntityMetadata<ByteArrayTest>({
        id(ByteArrayTest::id)
    })

    data class BigDecimalTest(val id: Int, val value: BigDecimal)
    object BigDecimalTestMetadata : EntityMetadata<BigDecimalTest>({
        id(BigDecimalTest::id)
    })

    data class BigIntegerTest(val id: Int, val value: BigInteger)
    object BigIntegerTestMetadata : EntityMetadata<BigIntegerTest>({
        id(BigIntegerTest::id)
    })

    data class BooleanTest(val id: Int, val value: Boolean)
    object BooleanTestMetadata : EntityMetadata<BooleanTest>({
        id(BooleanTest::id)
    })

    data class ByteTest(val id: Int, val value: Byte)
    object ByteTestMetadata : EntityMetadata<ByteTest>({
        id(ByteTest::id)
    })

    data class DoubleTest(val id: Int, val value: Double)
    object DoubleTestMetadata : EntityMetadata<DoubleTest>({
        id(DoubleTest::id)
    })

    data class EnumTest(val id: Int, val value: Direction)
    object EnumTestMetadata : EntityMetadata<EnumTest>({
        id(EnumTest::id)
    })

    data class FloatTest(val id: Int, val value: Float)
    object FloatTestMetadata : EntityMetadata<FloatTest>({
        id(FloatTest::id)
    })

    data class IntTest(val id: Int, val value: Int)
    object IntTestMetadata : EntityMetadata<IntTest>({
        id(IntTest::id)
    })

    data class LocalDateTest(val id: Int, val value: LocalDate)
    object LocalDateTestMetadata : EntityMetadata<LocalDateTest>({
        id(LocalDateTest::id)
    })

    data class LocalDateTimeTest(val id: Int, val value: LocalDateTime)
    object LocalDateTimeTestMetadata : EntityMetadata<LocalDateTimeTest>({
        id(LocalDateTimeTest::id)
    })

    data class LocalTimeTest(val id: Int, val value: LocalTime)
    object LocalTimeTestMetadata : EntityMetadata<LocalTimeTest>({
        id(LocalTimeTest::id)
    })

    data class LongTest(val id: Int, val value: Long)
    object LongTestMetadata : EntityMetadata<LongTest>({
        id(LongTest::id)
    })

    data class OffsetDateTimeTest(val id: Int, val value: OffsetDateTime)
    object OffsetDateTimeTestMetadata : EntityMetadata<OffsetDateTimeTest>({
        id(OffsetDateTimeTest::id)
    })

    data class ShortTest(val id: Int, val value: Short)
    object ShortTestMetadata : EntityMetadata<ShortTest>({
        id(ShortTest::id)
    })

    data class StringTest(val id: Int, val value: String)
    object StringTestMetadata : EntityMetadata<StringTest>({
        id(StringTest::id)
    })

    @Test
    fun bigDecimal(db: Db) {
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.insert(data)
        val data2 = db.findById<BigDecimalTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger(db: Db) {
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.insert(data)
        val data2 = db.findById<BigIntegerTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun boolean(db: Db) {
        val data = BooleanTest(1, true)
        db.insert(data)
        val data2 = db.findById<BooleanTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun byte(db: Db) {
        val data = ByteTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ByteTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun byteArray(db: Db) {
        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.insert(data)
        val data2 = db.findById<ByteArrayTest>(1)
        assertEquals(data.id, data2!!.id)
        assertArrayEquals(data.value, data2.value)
    }

    @Test
    fun double(db: Db) {
        val data = DoubleTest(1, 10.0)
        db.insert(data)
        val data2 = db.findById<DoubleTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun enum(db: Db) {
        val data = EnumTest(1, Direction.EAST)
        db.insert(data)
        val data2 = db.findById<EnumTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun float(db: Db) {
        val data = FloatTest(1, 10.0f)
        db.insert(data)
        val data2 = db.findById<FloatTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun int(db: Db) {
        val data = IntTest(1, 10)
        db.insert(data)
        val data2 = db.findById<IntTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime(db: Db) {
        val data = LocalDateTimeTest(1, LocalDateTime.of(2019, 6, 1, 12, 11, 10))
        db.insert(data)
        val data2 = db.findById<LocalDateTimeTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localDate(db: Db) {
        val data = LocalDateTest(1, LocalDate.of(2019, 6, 1))
        db.insert(data)
        val data2 = db.findById<LocalDateTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localTime(db: Db) {
        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.insert(data)
        val data2 = db.findById<LocalTimeTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun long(db: Db) {
        val data = LongTest(1, 10L)
        db.insert(data)
        val data2 = db.findById<LongTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun offsetDateTime(db: Db) {
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
        val data = ShortTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ShortTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun string(db: Db) {
        val data = StringTest(1, "ABC")
        db.insert(data)
        val data2 = db.findById<StringTest>(1)
        assertEquals(data, data2)
    }
}
