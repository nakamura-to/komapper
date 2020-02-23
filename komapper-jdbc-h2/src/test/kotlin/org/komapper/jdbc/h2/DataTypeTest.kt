package org.komapper.jdbc.h2

import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.entity.EntityMeta
import org.komapper.core.entity.EntityMetaResolver
import org.komapper.core.entity.IdMeta

@ExtendWith(Env::class)
internal class DataTypeTest(_db: Db) {

    class DataTypeEntityMetaResolver : EntityMetaResolver {
        override fun <T : Any> resolve(kClass: KClass<T>): EntityMeta<T> {
            val id = kClass.memberProperties.first { it.name == "id" }.let { IdMeta.Assign(it.name) }
            return EntityMeta(kClass, idList = listOf(id))
        }
    }

    val db = Db(object : DbConfig() {
        override val dataSource = _db.config.dataSource
        override val dialect = _db.config.dialect
        override val entityMetaResolver = DataTypeEntityMetaResolver()
    })

    @Test
    fun any() {
        data class AnyPerson(val name: String) : Serializable
        data class AnyTest(val id: Int, val value: Any)

        val data = AnyTest(
            1,
            AnyPerson("ABC")
        )
        db.insert(data)
        val data2 = db.findById<AnyTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun bigDecimal() {
        data class BigDecimalTest(val id: Int, val value: BigDecimal)

        val data = BigDecimalTest(1, BigDecimal.TEN)
        db.insert(data)
        val data2 = db.findById<BigDecimalTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        data class BigIntegerTest(val id: Int, val value: BigInteger)

        val data = BigIntegerTest(1, BigInteger.TEN)
        db.insert(data)
        val data2 = db.findById<BigIntegerTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun boolean() {
        data class BooleanTest(val id: Int, val value: Boolean)

        val data = BooleanTest(1, true)
        db.insert(data)
        val data2 = db.findById<BooleanTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun byte() {
        data class ByteTest(val id: Int, val value: Byte)

        val data = ByteTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ByteTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
        @Suppress("ArrayInDataClass")
        data class ByteArrayTest(val id: Int, val value: ByteArray)

        val data = ByteArrayTest(1, byteArrayOf(10, 20, 30))
        db.insert(data)
        val data2 = db.findById<ByteArrayTest>(1)
        Assertions.assertEquals(data.id, data2!!.id)
        Assertions.assertArrayEquals(data.value, data2.value)
    }

    @Test
    fun double() {
        data class DoubleTest(val id: Int, val value: Double)

        val data = DoubleTest(1, 10.0)
        db.insert(data)
        val data2 = db.findById<DoubleTest>(1)
        Assertions.assertEquals(data, data2)
    }

    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    @Test
    fun enum() {
        data class EnumTest(val id: Int, val value: Direction)

        val data = EnumTest(
            1,
            Direction.EAST
        )
        db.insert(data)
        val data2 = db.findById<EnumTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun float() {
        data class FloatTest(val id: Int, val value: Float)

        val data = FloatTest(1, 10.0f)
        db.insert(data)
        val data2 = db.findById<FloatTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun int() {
        data class IntTest(val id: Int, val value: Int)

        val data = IntTest(1, 10)
        db.insert(data)
        val data2 = db.findById<IntTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        data class LocalDateTimeTest(val id: Int, val value: LocalDateTime)

        val data = LocalDateTimeTest(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        )
        db.insert(data)
        val data2 = db.findById<LocalDateTimeTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun localDate() {
        data class LocalDateTest(val id: Int, val value: LocalDate)

        val data = LocalDateTest(1, LocalDate.of(2019, 6, 1))
        db.insert(data)
        val data2 = db.findById<LocalDateTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun localTime() {
        data class LocalTimeTest(val id: Int, val value: LocalTime)

        val data = LocalTimeTest(1, LocalTime.of(12, 11, 10))
        db.insert(data)
        val data2 = db.findById<LocalTimeTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun long() {
        data class LongTest(val id: Int, val value: Long)

        val data = LongTest(1, 10L)
        db.insert(data)
        val data2 = db.findById<LongTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun offsetDateTime() {
        data class OffsetDateTimeTest(val id: Int, val value: OffsetDateTime)

        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(9)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeTest(1, value)
        db.insert(data)
        val data2 = db.findById<OffsetDateTimeTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun short() {
        data class ShortTest(val id: Int, val value: Short)

        val data = ShortTest(1, 10)
        db.insert(data)
        val data2 = db.findById<ShortTest>(1)
        Assertions.assertEquals(data, data2)
    }

    @Test
    fun string() {
        data class StringTest(val id: Int, val value: String)

        val data = StringTest(1, "ABC")
        db.insert(data)
        val data2 = db.findById<StringTest>(1)
        Assertions.assertEquals(data, data2)
    }
}
