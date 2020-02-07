package org.komapper.core.criteria

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KProperty1
import org.komapper.core.dsl.Scope

typealias Where = WhereScope.() -> Unit

fun where(block: Where): Where = block

infix operator fun (Where).plus(other: Where): Where {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
@Suppress("FunctionName")
class WhereScope(val _add: (Criterion) -> Unit) {

    fun eqAny(prop: KProperty1<*, *>, value: Any?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Array?>, value: Array?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, BigDecimal?>, value: BigDecimal?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, BigInteger?>, value: BigInteger?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Blob?>, value: Blob?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Boolean?>, value: Boolean?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Byte?>, value: Byte?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, ByteArray?>, value: ByteArray?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Clob?>, value: Clob?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Double?>, value: Double?) = _eq(prop, value)
    fun <V : Enum<V>> eq(prop: KProperty1<*, V?>, value: V?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Float?>, value: Float?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Int?>, value: Int?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, LocalDateTime?>, value: LocalDateTime?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, LocalDate?>, value: LocalDate?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, LocalTime?>, value: LocalTime?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Long?>, value: Long?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, NClob?>, value: NClob?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, Short?>, value: Short?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, String?>, value: String?) = _eq(prop, value)
    fun eq(prop: KProperty1<*, SQLXML?>, value: SQLXML?) = _eq(prop, value)
    private fun _eq(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Eq(prop, value))

    fun neAny(prop: KProperty1<*, *>, value: Any?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Array?>, value: Array?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, BigDecimal?>, value: BigDecimal?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, BigInteger?>, value: BigInteger?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Blob?>, value: Blob?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Boolean?>, value: Boolean?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Byte?>, value: Byte?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, ByteArray?>, value: ByteArray?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Clob?>, value: Clob?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Double?>, value: Double?) = _ne(prop, value)
    fun <V : Enum<V>> ne(prop: KProperty1<*, V?>, value: V?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Float?>, value: Float?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Int?>, value: Int?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, LocalDateTime?>, value: LocalDateTime?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, LocalDate?>, value: LocalDate?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, LocalTime?>, value: LocalTime?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Long?>, value: Long?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, NClob?>, value: NClob?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, Short?>, value: Short?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, String?>, value: String?) = _ne(prop, value)
    fun ne(prop: KProperty1<*, SQLXML?>, value: SQLXML?) = _ne(prop, value)
    private fun _ne(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Ne(prop, value))

    fun gtAny(prop: KProperty1<*, *>, value: Any?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Array?>, value: Array?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, BigDecimal?>, value: BigDecimal?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, BigInteger?>, value: BigInteger?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Blob?>, value: Blob?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Boolean?>, value: Boolean?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Byte?>, value: Byte?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, ByteArray?>, value: ByteArray?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Clob?>, value: Clob?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Double?>, value: Double?) = _gt(prop, value)
    fun <V : Enum<V>> gt(prop: KProperty1<*, V?>, value: V?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Float?>, value: Float?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Int?>, value: Int?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, LocalDateTime?>, value: LocalDateTime?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, LocalDate?>, value: LocalDate?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, LocalTime?>, value: LocalTime?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Long?>, value: Long?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, NClob?>, value: NClob?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, Short?>, value: Short?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, String?>, value: String?) = _gt(prop, value)
    fun gt(prop: KProperty1<*, SQLXML?>, value: SQLXML?) = _gt(prop, value)
    private fun _gt(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Gt(prop, value))

    fun ltAny(prop: KProperty1<*, *>, value: Any?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Array?>, value: Array?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, BigDecimal?>, value: BigDecimal?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, BigInteger?>, value: BigInteger?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Blob?>, value: Blob?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Boolean?>, value: Boolean?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Byte?>, value: Byte?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, ByteArray?>, value: ByteArray?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Clob?>, value: Clob?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Double?>, value: Double?) = _lt(prop, value)
    fun <V : Enum<V>> lt(prop: KProperty1<*, V?>, value: V?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Float?>, value: Float?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Int?>, value: Int?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, LocalDateTime?>, value: LocalDateTime?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, LocalDate?>, value: LocalDate?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, LocalTime?>, value: LocalTime?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Long?>, value: Long?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, NClob?>, value: NClob?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, Short?>, value: Short?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, String?>, value: String?) = _lt(prop, value)
    fun lt(prop: KProperty1<*, SQLXML?>, value: SQLXML?) = _lt(prop, value)
    private fun _lt(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Lt(prop, value))

    fun geAny(prop: KProperty1<*, *>, value: Any?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Array?>, value: Array?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, BigDecimal?>, value: BigDecimal?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, BigInteger?>, value: BigInteger?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Blob?>, value: Blob?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Boolean?>, value: Boolean?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Byte?>, value: Byte?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, ByteArray?>, value: ByteArray?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Clob?>, value: Clob?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Double?>, value: Double?) = _ge(prop, value)
    fun <V : Enum<V>> ge(prop: KProperty1<*, V?>, value: V?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Float?>, value: Float?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Int?>, value: Int?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, LocalDateTime?>, value: LocalDateTime?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, LocalDate?>, value: LocalDate?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, LocalTime?>, value: LocalTime?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Long?>, value: Long?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, NClob?>, value: NClob?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, Short?>, value: Short?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, String?>, value: String?) = _ge(prop, value)
    fun ge(prop: KProperty1<*, SQLXML?>, value: SQLXML?) = _ge(prop, value)
    private fun _ge(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Ge(prop, value))

    fun leAny(prop: KProperty1<*, *>, value: Any?) = _le(prop, value)
    fun le(prop: KProperty1<*, Array?>, value: Array?) = _le(prop, value)
    fun le(prop: KProperty1<*, BigDecimal?>, value: BigDecimal?) = _le(prop, value)
    fun le(prop: KProperty1<*, BigInteger?>, value: BigInteger?) = _le(prop, value)
    fun le(prop: KProperty1<*, Blob?>, value: Blob?) = _le(prop, value)
    fun le(prop: KProperty1<*, Boolean?>, value: Boolean?) = _le(prop, value)
    fun le(prop: KProperty1<*, Byte?>, value: Byte?) = _le(prop, value)
    fun le(prop: KProperty1<*, ByteArray?>, value: ByteArray?) = _le(prop, value)
    fun le(prop: KProperty1<*, Clob?>, value: Clob?) = _le(prop, value)
    fun le(prop: KProperty1<*, Double?>, value: Double?) = _le(prop, value)
    fun <V : Enum<V>> le(prop: KProperty1<*, V?>, value: V?) = _le(prop, value)
    fun le(prop: KProperty1<*, Float?>, value: Float?) = _le(prop, value)
    fun le(prop: KProperty1<*, Int?>, value: Int?) = _le(prop, value)
    fun le(prop: KProperty1<*, LocalDateTime?>, value: LocalDateTime?) = _le(prop, value)
    fun le(prop: KProperty1<*, LocalDate?>, value: LocalDate?) = _le(prop, value)
    fun le(prop: KProperty1<*, LocalTime?>, value: LocalTime?) = _le(prop, value)
    fun le(prop: KProperty1<*, Long?>, value: Long?) = _le(prop, value)
    fun le(prop: KProperty1<*, NClob?>, value: NClob?) = _le(prop, value)
    fun le(prop: KProperty1<*, Short?>, value: Short?) = _le(prop, value)
    fun le(prop: KProperty1<*, String?>, value: String?) = _le(prop, value)
    fun le(prop: KProperty1<*, SQLXML?>, value: SQLXML?) = _le(prop, value)
    private fun _le(prop: KProperty1<*, *>, value: Any?) = _add(Criterion.Le(prop, value))

    fun inAny(
        prop: KProperty1<*, *>,
        value: List<Any?>
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Array?>,
        value: List<Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, BigDecimal?>,
        value: List<BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, BigInteger?>,
        value: List<BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Blob?>,
        value: List<Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Byte?>,
        value: List<Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, ByteArray?>,
        value: List<ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Clob?>,
        value: List<Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Double?>,
        value: List<Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _in(prop, value)

    fun <V : Enum<V>> `in`(
        prop: KProperty1<*, V?>,
        value: List<V?>,
        @Suppress(
            "UNUSED_PARAMETER"
        ) `_`: V? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Float?>,
        value: List<Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Int?>,
        value: List<Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, LocalDateTime?>,
        value: List<LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, LocalDate?>,
        value: List<LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, LocalTime?>,
        value: List<LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Long?>,
        value: List<Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, NClob?>,
        value: List<NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, Short?>,
        value: List<Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, String?>,
        value: List<String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _in(prop, value)

    fun `in`(
        prop: KProperty1<*, SQLXML?>,
        value: List<SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _notIn(prop, value)

    private fun _in(prop: KProperty1<*, *>, value: List<Any?>) {
        _add(Criterion.In(prop, value))
    }

    fun <A, B> `in`(props: Pair<KProperty1<*, A>, KProperty1<*, B>>, value: List<Pair<A, B>>) {
        _add(Criterion.In2(props, value))
    }

    fun <A, B, C> `in`(
        props: Triple<KProperty1<*, A>, KProperty1<*, B>, KProperty1<*, C>>,
        value: List<Triple<A, B, C>>
    ) = _add(Criterion.In3(props, value))

    fun <V : Any> notInAny(
        prop: KProperty1<*, V>,
        value: List<Any?>
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Array?>,
        value: List<Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, BigDecimal?>,
        value: List<BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, BigInteger?>,
        value: List<BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Blob?>,
        value: List<Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Byte?>,
        value: List<Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, ByteArray?>,
        value: List<ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Clob?>,
        value: List<Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Double?>,
        value: List<Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _notIn(prop, value)

    fun <V : Enum<V>> notIn(
        prop: KProperty1<*, V?>,
        value: List<V?>,
        @Suppress(
            "UNUSED_PARAMETER"
        ) `_`: V? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Float?>,
        value: List<Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Int?>,
        value: List<Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, LocalDateTime?>,
        value: List<LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, LocalDate?>,
        value: List<LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, LocalTime?>,
        value: List<LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Long?>,
        value: List<Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, NClob?>,
        value: List<NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, Short?>,
        value: List<Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, String?>,
        value: List<String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _notIn(prop, value)

    fun notIn(
        prop: KProperty1<*, SQLXML?>,
        value: List<SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _notIn(prop, value)

    private fun _notIn(prop: KProperty1<*, *>, value: List<Any?>) {
        _add(Criterion.NotIn(prop, value))
    }

    fun <A, B> notIn(
        pair: Pair<KProperty1<*, A>, KProperty1<*, B>>,
        value: List<Pair<A, B>>
    ) = _add(Criterion.NotIn2(pair, value))

    fun <A, B, C> notIn(
        triple: Triple<KProperty1<*, A>, KProperty1<*, B>, KProperty1<*, C>>,
        value: List<Triple<A, B, C>>
    ) = _add(Criterion.NotIn3(triple, value))

    fun betweenAny(prop: KProperty1<*, *>, begin: Any?, end: Any?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Array?>, begin: Array?, end: Array?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, BigDecimal?>, begin: BigDecimal?, end: BigDecimal?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, BigInteger?>, begin: BigInteger?, end: BigInteger?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Blob?>, begin: Blob?, end: Blob?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Boolean?>, begin: Boolean?, end: Boolean?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Byte?>, begin: Byte?, end: Byte?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, ByteArray?>, begin: ByteArray?, end: ByteArray?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Clob?>, begin: Clob?, end: Clob?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Double?>, begin: Double?, end: Double?) = _between(prop, begin, end)
    fun <V : Enum<V>> between(prop: KProperty1<*, V?>, begin: V?, end: V?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Float?>, begin: Float?, end: Float?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Int?>, begin: Int?, end: Int?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, LocalDateTime?>, begin: LocalDateTime?, end: LocalDateTime?) =
        _between(prop, begin, end)

    fun between(prop: KProperty1<*, LocalDate?>, begin: LocalDate?, end: LocalDate) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, LocalTime?>, begin: LocalTime?, end: LocalTime) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Long?>, begin: Long?, end: Long?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, NClob?>, begin: NClob?, end: NClob?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, Short?>, begin: Short?, end: Short?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, String?>, begin: String?, end: String?) = _between(prop, begin, end)
    fun between(prop: KProperty1<*, SQLXML?>, begin: SQLXML?, end: SQLXML) = _between(prop, begin, end)
    private fun _between(prop: KProperty1<*, *>, begin: Any?, end: Any?) =
        _add(Criterion.Between(prop, begin to end))

    fun like(prop: KProperty1<*, String?>, value: String?) = _add(Criterion.Like(prop, value))

    fun notLike(prop: KProperty1<*, String?>, value: String?) = _add(Criterion.NotLike(prop, value))

    fun not(block: Where) = runBlock(block, Criterion::Not)

    fun and(block: Where) = runBlock(block, Criterion::And)

    fun or(block: Where) = runBlock(block, Criterion::Or)

    private fun runBlock(block: Where, factory: (List<Criterion>) -> Criterion) {
        val criterionList = mutableListOf<Criterion>()
        WhereScope { criterionList.add(it) }.block()
        if (criterionList.isNotEmpty()) {
            _add(factory(criterionList))
        }
    }
}
