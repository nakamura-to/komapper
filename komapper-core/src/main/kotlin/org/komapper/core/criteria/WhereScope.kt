package org.komapper.core.criteria

import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KProperty1

@CriteriaMarker
@Suppress("FunctionName")
class WhereScope {

    internal val criterionList = ArrayList<Criterion>()

    infix fun KProperty1<*, Any?>.eq(value: () -> Any?) = _eq(this, value())
    infix fun KProperty1<*, java.sql.Array?>.eq(value: java.sql.Array?) = _eq(this, value)
    infix fun KProperty1<*, BigDecimal?>.eq(value: BigDecimal?) = _eq(this, value)
    infix fun KProperty1<*, BigInteger?>.eq(value: BigInteger?) = _eq(this, value)
    infix fun KProperty1<*, Blob?>.eq(value: Blob?) = _eq(this, value)
    infix fun KProperty1<*, Boolean?>.eq(value: Boolean?) = _eq(this, value)
    infix fun KProperty1<*, Byte?>.eq(value: Byte?) = _eq(this, value)
    infix fun KProperty1<*, ByteArray?>.eq(value: ByteArray?) = _eq(this, value)
    infix fun KProperty1<*, Clob?>.eq(value: Clob?) = _eq(this, value)
    infix fun KProperty1<*, Double?>.eq(value: Double?) = _eq(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.eq(value: V?) = _eq(this, value)
    infix fun KProperty1<*, Float?>.eq(value: Float?) = _eq(this, value)
    infix fun KProperty1<*, Int?>.eq(value: Int?) = _eq(this, value)
    infix fun KProperty1<*, LocalDateTime?>.eq(value: LocalDateTime?) = _eq(this, value)
    infix fun KProperty1<*, LocalDate?>.eq(value: LocalDate?) = _eq(this, value)
    infix fun KProperty1<*, LocalTime?>.eq(value: LocalTime?) = _eq(this, value)
    infix fun KProperty1<*, Long?>.eq(value: Long?) = _eq(this, value)
    infix fun KProperty1<*, NClob?>.eq(value: NClob?) = _eq(this, value)
    infix fun KProperty1<*, Short?>.eq(value: Short?) = _eq(this, value)
    infix fun KProperty1<*, String?>.eq(value: String?) = _eq(this, value)
    infix fun KProperty1<*, SQLXML?>.eq(value: SQLXML?) = _eq(this, value)
    private fun _eq(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Eq(prop, value))
    }

    infix fun KProperty1<*, Any?>.ne(value: () -> Any?) = _ne(this, value())
    infix fun KProperty1<*, java.sql.Array?>.ne(value: java.sql.Array?) = _ne(this, value)
    infix fun KProperty1<*, BigDecimal?>.ne(value: BigDecimal?) = _ne(this, value)
    infix fun KProperty1<*, BigInteger?>.ne(value: BigInteger?) = _ne(this, value)
    infix fun KProperty1<*, Blob?>.ne(value: Blob?) = _ne(this, value)
    infix fun KProperty1<*, Boolean?>.ne(value: Boolean?) = _ne(this, value)
    infix fun KProperty1<*, Byte?>.ne(value: Byte?) = _ne(this, value)
    infix fun KProperty1<*, ByteArray?>.ne(value: ByteArray?) = _ne(this, value)
    infix fun KProperty1<*, Clob?>.ne(value: Clob?) = _ne(this, value)
    infix fun KProperty1<*, Double?>.ne(value: Double?) = _ne(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.ne(value: V?) = _ne(this, value)
    infix fun KProperty1<*, Float?>.ne(value: Float?) = _ne(this, value)
    infix fun KProperty1<*, Int?>.ne(value: Int?) = _ne(this, value)
    infix fun KProperty1<*, LocalDateTime?>.ne(value: LocalDateTime?) = _ne(this, value)
    infix fun KProperty1<*, LocalDate?>.ne(value: LocalDate?) = _ne(this, value)
    infix fun KProperty1<*, LocalTime?>.ne(value: LocalTime?) = _ne(this, value)
    infix fun KProperty1<*, Long?>.ne(value: Long?) = _ne(this, value)
    infix fun KProperty1<*, NClob?>.ne(value: NClob?) = _ne(this, value)
    infix fun KProperty1<*, Short?>.ne(value: Short?) = _ne(this, value)
    infix fun KProperty1<*, String?>.ne(value: String?) = _ne(this, value)
    infix fun KProperty1<*, SQLXML?>.ne(value: SQLXML?) = _ne(this, value)
    private fun _ne(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Ne(prop, value))
    }

    infix fun KProperty1<*, Any?>.gt(value: () -> Any?) = _gt(this, value())
    infix fun KProperty1<*, java.sql.Array?>.gt(value: java.sql.Array?) = _gt(this, value)
    infix fun KProperty1<*, BigDecimal?>.gt(value: BigDecimal?) = _gt(this, value)
    infix fun KProperty1<*, BigInteger?>.gt(value: BigInteger?) = _gt(this, value)
    infix fun KProperty1<*, Blob?>.gt(value: Blob?) = _gt(this, value)
    infix fun KProperty1<*, Boolean?>.gt(value: Boolean?) = _gt(this, value)
    infix fun KProperty1<*, Byte?>.gt(value: Byte?) = _gt(this, value)
    infix fun KProperty1<*, ByteArray?>.gt(value: ByteArray?) = _gt(this, value)
    infix fun KProperty1<*, Clob?>.gt(value: Clob?) = _gt(this, value)
    infix fun KProperty1<*, Double?>.gt(value: Double?) = _gt(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.gt(value: V?) = _gt(this, value)
    infix fun KProperty1<*, Float?>.gt(value: Float?) = _gt(this, value)
    infix fun KProperty1<*, Int?>.gt(value: Int?) = _gt(this, value)
    infix fun KProperty1<*, LocalDateTime?>.gt(value: LocalDateTime?) = _gt(this, value)
    infix fun KProperty1<*, LocalDate?>.gt(value: LocalDate?) = _gt(this, value)
    infix fun KProperty1<*, LocalTime?>.gt(value: LocalTime?) = _gt(this, value)
    infix fun KProperty1<*, Long?>.gt(value: Long?) = _gt(this, value)
    infix fun KProperty1<*, NClob?>.gt(value: NClob?) = _gt(this, value)
    infix fun KProperty1<*, Short?>.gt(value: Short?) = _gt(this, value)
    infix fun KProperty1<*, String?>.gt(value: String?) = _gt(this, value)
    infix fun KProperty1<*, SQLXML?>.gt(value: SQLXML?) = _gt(this, value)
    private fun _gt(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Gt(prop, value))
    }

    infix fun KProperty1<*, Any?>.ge(value: () -> Any?) = _ge(this, value())
    infix fun KProperty1<*, java.sql.Array?>.ge(value: java.sql.Array?) = _ge(this, value)
    infix fun KProperty1<*, BigDecimal?>.ge(value: BigDecimal?) = _ge(this, value)
    infix fun KProperty1<*, BigInteger?>.ge(value: BigInteger?) = _ge(this, value)
    infix fun KProperty1<*, Blob?>.ge(value: Blob?) = _ge(this, value)
    infix fun KProperty1<*, Boolean?>.ge(value: Boolean?) = _ge(this, value)
    infix fun KProperty1<*, Byte?>.ge(value: Byte?) = _ge(this, value)
    infix fun KProperty1<*, ByteArray?>.ge(value: ByteArray?) = _ge(this, value)
    infix fun KProperty1<*, Clob?>.ge(value: Clob?) = _ge(this, value)
    infix fun KProperty1<*, Double?>.ge(value: Double?) = _ge(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.ge(value: V?) = _ge(this, value)
    infix fun KProperty1<*, Float?>.ge(value: Float?) = _ge(this, value)
    infix fun KProperty1<*, Int?>.ge(value: Int?) = _ge(this, value)
    infix fun KProperty1<*, LocalDateTime?>.ge(value: LocalDateTime?) = _ge(this, value)
    infix fun KProperty1<*, LocalDate?>.ge(value: LocalDate?) = _ge(this, value)
    infix fun KProperty1<*, LocalTime?>.ge(value: LocalTime?) = _ge(this, value)
    infix fun KProperty1<*, Long?>.ge(value: Long?) = _ge(this, value)
    infix fun KProperty1<*, NClob?>.ge(value: NClob?) = _ge(this, value)
    infix fun KProperty1<*, Short?>.ge(value: Short?) = _ge(this, value)
    infix fun KProperty1<*, String?>.ge(value: String?) = _ge(this, value)
    infix fun KProperty1<*, SQLXML?>.ge(value: SQLXML?) = _ge(this, value)
    private fun _ge(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Ge(prop, value))
    }

    infix fun KProperty1<*, Any?>.lt(value: () -> Any?) = _lt(this, value())
    infix fun KProperty1<*, java.sql.Array?>.lt(value: java.sql.Array?) = _lt(this, value)
    infix fun KProperty1<*, BigDecimal?>.lt(value: BigDecimal?) = _lt(this, value)
    infix fun KProperty1<*, BigInteger?>.lt(value: BigInteger?) = _lt(this, value)
    infix fun KProperty1<*, Blob?>.lt(value: Blob?) = _lt(this, value)
    infix fun KProperty1<*, Boolean?>.lt(value: Boolean?) = _lt(this, value)
    infix fun KProperty1<*, Byte?>.lt(value: Byte?) = _lt(this, value)
    infix fun KProperty1<*, ByteArray?>.lt(value: ByteArray?) = _lt(this, value)
    infix fun KProperty1<*, Clob?>.lt(value: Clob?) = _lt(this, value)
    infix fun KProperty1<*, Double?>.lt(value: Double?) = _lt(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.lt(value: V?) = _lt(this, value)
    infix fun KProperty1<*, Float?>.lt(value: Float?) = _lt(this, value)
    infix fun KProperty1<*, Int?>.lt(value: Int?) = _lt(this, value)
    infix fun KProperty1<*, LocalDateTime?>.lt(value: LocalDateTime?) = _lt(this, value)
    infix fun KProperty1<*, LocalDate?>.lt(value: LocalDate?) = _lt(this, value)
    infix fun KProperty1<*, LocalTime?>.lt(value: LocalTime?) = _lt(this, value)
    infix fun KProperty1<*, Long?>.lt(value: Long?) = _lt(this, value)
    infix fun KProperty1<*, NClob?>.lt(value: NClob?) = _lt(this, value)
    infix fun KProperty1<*, Short?>.lt(value: Short?) = _lt(this, value)
    infix fun KProperty1<*, String?>.lt(value: String?) = _lt(this, value)
    infix fun KProperty1<*, SQLXML?>.lt(value: SQLXML?) = _lt(this, value)
    private fun _lt(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Lt(prop, value))
    }

    infix fun KProperty1<*, Any?>.le(value: () -> Any?) = _le(this, value())
    infix fun KProperty1<*, java.sql.Array?>.le(value: java.sql.Array?) = _le(this, value)
    infix fun KProperty1<*, BigDecimal?>.le(value: BigDecimal?) = _le(this, value)
    infix fun KProperty1<*, BigInteger?>.le(value: BigInteger?) = _le(this, value)
    infix fun KProperty1<*, Blob?>.le(value: Blob?) = _le(this, value)
    infix fun KProperty1<*, Boolean?>.le(value: Boolean?) = _le(this, value)
    infix fun KProperty1<*, Byte?>.le(value: Byte?) = _le(this, value)
    infix fun KProperty1<*, ByteArray?>.le(value: ByteArray?) = _le(this, value)
    infix fun KProperty1<*, Clob?>.le(value: Clob?) = _le(this, value)
    infix fun KProperty1<*, Double?>.le(value: Double?) = _le(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.le(value: V?) = _le(this, value)
    infix fun KProperty1<*, Float?>.le(value: Float?) = _le(this, value)
    infix fun KProperty1<*, Int?>.le(value: Int?) = _le(this, value)
    infix fun KProperty1<*, LocalDateTime?>.le(value: LocalDateTime?) = _le(this, value)
    infix fun KProperty1<*, LocalDate?>.le(value: LocalDate?) = _le(this, value)
    infix fun KProperty1<*, LocalTime?>.le(value: LocalTime?) = _le(this, value)
    infix fun KProperty1<*, Long?>.le(value: Long?) = _le(this, value)
    infix fun KProperty1<*, NClob?>.le(value: NClob?) = _le(this, value)
    infix fun KProperty1<*, Short?>.le(value: Short?) = _le(this, value)
    infix fun KProperty1<*, String?>.le(value: String?) = _le(this, value)
    infix fun KProperty1<*, SQLXML?>.le(value: SQLXML?) = _le(this, value)
    private fun _le(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Le(prop, value))
    }

    infix fun KProperty1<*, String?>.like(value: String?) {
        criterionList.add(Criterion.Like(this, value))
    }

    infix fun KProperty1<*, String?>.notLike(value: String?) {
        criterionList.add(Criterion.NotLike(this, value))
    }

    infix fun <V> KProperty1<*, V>.`in`(value: List<V>) {
        criterionList.add(Criterion.In(this, value))
    }

    infix fun <V> KProperty1<*, V>.notIn(value: List<V>) {
        criterionList.add(Criterion.NotIn(this, value))
    }

    infix fun <V> KProperty1<*, V>.between(pair: Pair<V, V>) {
        criterionList.add(Criterion.Between(this, pair))
    }

    fun not(block: WhereScope.() -> Unit) {
        val scope = WhereScope()
        scope.block()
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.Not(scope.criterionList))
        }
    }

    fun and(block: WhereScope.() -> Unit) {
        val scope = WhereScope()
        scope.block()
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.And(scope.criterionList))
        }
    }

    fun or(block: WhereScope.() -> Unit) {
        val scope = WhereScope()
        scope.block()
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.Or(scope.criterionList))
        }
    }
}
