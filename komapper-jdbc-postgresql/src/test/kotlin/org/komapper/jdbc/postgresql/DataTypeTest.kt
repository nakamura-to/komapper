package org.komapper.jdbc.postgresql

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.entity.EntityMeta
import org.komapper.core.entity.EntityMetaResolver
import org.komapper.core.entity.IdMeta

@ExtendWith(Env::class)
class DataTypeTest {

    @Suppress("unused")
    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    class DataTypeEntityMetaResolver : EntityMetaResolver {
        override fun <T : Any> resolve(kClass: KClass<T>): EntityMeta<T> {
            val id = kClass.memberProperties.first { it.name == "id" }.let { IdMeta.Assign(it.name) }
            return EntityMeta(kClass, idList = listOf(id))
        }
    }

    private fun newDb(db: Db) = Db(object : DbConfig() {
        override val dataSource = db.config.dataSource
        override val dialect = db.config.dialect
        override val entityMetaResolver = DataTypeEntityMetaResolver()
    })

    @Test
    fun bigDecimal(db: Db) {
        data class BigDecimalTest(val id: Int, val value: BigDecimal)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.insert(data)
        val data2 = db.findById<BigDecimalTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger(db: Db) {
        data class BigIntegerTest(val id: Int, val value: BigInteger)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = BigIntegerTest(1, BigInteger.TEN)
        db.insert(data)
        val data2 = db.findById<BigIntegerTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun boolean(db: Db) {
        data class BooleanTest(val id: Int, val value: Boolean)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = BooleanTest(1, true)
        db.insert(data)
        val data2 = db.findById<BooleanTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun byte(db: Db) {
        data class ByteTest(val id: Int, val value: Byte)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = ByteTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ByteTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun byteArray(db: Db) {
        @Suppress("ArrayInDataClass")
        data class ByteArrayTest(val id: Int, val value: ByteArray)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data =
            ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.insert(data)
        val data2 = db.findById<ByteArrayTest>(1)
        assertEquals(data.id, data2!!.id)
        assertArrayEquals(data.value, data2.value)
    }

    @Test
    fun double(db: Db) {
        data class DoubleTest(val id: Int, val value: Double)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = DoubleTest(1, 10.0)
        db.insert(data)
        val data2 = db.findById<DoubleTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun enum(db: Db) {
        data class EnumTest(val id: Int, val value: Direction)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = EnumTest(
            1,
            Direction.EAST
        )
        db.insert(data)
        val data2 = db.findById<EnumTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun float(db: Db) {
        data class FloatTest(val id: Int, val value: Float)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = FloatTest(1, 10.0f)
        db.insert(data)
        val data2 = db.findById<FloatTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun int(db: Db) {
        data class IntTest(val id: Int, val value: Int)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = IntTest(1, 10)
        db.insert(data)
        val data2 = db.findById<IntTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime(db: Db) {
        data class LocalDateTimeTest(val id: Int, val value: LocalDateTime)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = LocalDateTimeTest(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        )
        db.insert(data)
        val data2 = db.findById<LocalDateTimeTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localDate(db: Db) {
        data class LocalDateTest(val id: Int, val value: LocalDate)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data =
            LocalDateTest(1, LocalDate.of(2019, 6, 1))
        db.insert(data)
        val data2 = db.findById<LocalDateTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun localTime(db: Db) {
        data class LocalTimeTest(val id: Int, val value: LocalTime)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data =
            LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.insert(data)
        val data2 = db.findById<LocalTimeTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun long(db: Db) {
        data class LongTest(val id: Int, val value: Long)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = LongTest(1, 10L)
        db.insert(data)
        val data2 = db.findById<LongTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun offsetDateTime(db: Db) {
        data class OffsetDateTimeTest(val id: Int, val value: OffsetDateTime)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
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
        data class ShortTest(val id: Int, val value: Short)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = ShortTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ShortTest>(1)
        assertEquals(data, data2)
    }

    @Test
    fun string(db: Db) {
        data class StringTest(val id: Int, val value: String)

        @Suppress("NAME_SHADOWING")
        val db = newDb(db)
        val data = StringTest(1, "ABC")
        db.insert(data)
        val data2 = db.findById<StringTest>(1)
        assertEquals(data, data2)
    }
}
