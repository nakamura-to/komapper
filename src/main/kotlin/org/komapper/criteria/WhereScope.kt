package org.komapper.criteria

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

@Suppress("FunctionName")
class WhereScope<T> {

    internal val criterionList = ArrayList<Criterion>()

    infix fun KProperty1<T, Any?>.eq(value: () -> Any?) = _eq(this, value())
    infix fun KProperty1<T, java.sql.Array?>.eq(value: java.sql.Array?) = _eq(this, value)
    infix fun KProperty1<T, BigDecimal?>.eq(value: BigDecimal?) = _eq(this, value)
    infix fun KProperty1<T, BigInteger?>.eq(value: BigInteger?) = _eq(this, value)
    infix fun KProperty1<T, Blob?>.eq(value: Blob?) = _eq(this, value)
    infix fun KProperty1<T, Boolean?>.eq(value: Boolean?) = _eq(this, value)
    infix fun KProperty1<T, Byte?>.eq(value: Byte?) = _eq(this, value)
    infix fun KProperty1<T, ByteArray?>.eq(value: ByteArray?) = _eq(this, value)
    infix fun KProperty1<T, Clob?>.eq(value: Clob?) = _eq(this, value)
    infix fun KProperty1<T, Double?>.eq(value: Double?) = _eq(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.eq(value: V?) = _eq(this, value)
    infix fun KProperty1<T, Float?>.eq(value: Float?) = _eq(this, value)
    infix fun KProperty1<T, Int?>.eq(value: Int?) = _eq(this, value)
    infix fun KProperty1<T, LocalDateTime?>.eq(value: LocalDateTime?) = _eq(this, value)
    infix fun KProperty1<T, LocalDate?>.eq(value: LocalDate?) = _eq(this, value)
    infix fun KProperty1<T, LocalTime?>.eq(value: LocalTime?) = _eq(this, value)
    infix fun KProperty1<T, Long?>.eq(value: Long?) = _eq(this, value)
    infix fun KProperty1<T, NClob?>.eq(value: NClob?) = _eq(this, value)
    infix fun KProperty1<T, Short?>.eq(value: Short?) = _eq(this, value)
    infix fun KProperty1<T, String?>.eq(value: String?) = _eq(this, value)
    infix fun KProperty1<T, SQLXML?>.eq(value: SQLXML?) = _eq(this, value)
    private fun _eq(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Eq(prop, value))
    }

    infix fun KProperty1<T, Any?>.ne(value: () -> Any?) = _ne(this, value())
    infix fun KProperty1<T, java.sql.Array?>.ne(value: java.sql.Array?) = _ne(this, value)
    infix fun KProperty1<T, BigDecimal?>.ne(value: BigDecimal?) = _ne(this, value)
    infix fun KProperty1<T, BigInteger?>.ne(value: BigInteger?) = _ne(this, value)
    infix fun KProperty1<T, Blob?>.ne(value: Blob?) = _ne(this, value)
    infix fun KProperty1<T, Boolean?>.ne(value: Boolean?) = _ne(this, value)
    infix fun KProperty1<T, Byte?>.ne(value: Byte?) = _ne(this, value)
    infix fun KProperty1<T, ByteArray?>.ne(value: ByteArray?) = _ne(this, value)
    infix fun KProperty1<T, Clob?>.ne(value: Clob?) = _ne(this, value)
    infix fun KProperty1<T, Double?>.ne(value: Double?) = _ne(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.ne(value: V?) = _ne(this, value)
    infix fun KProperty1<T, Float?>.ne(value: Float?) = _ne(this, value)
    infix fun KProperty1<T, Int?>.ne(value: Int?) = _ne(this, value)
    infix fun KProperty1<T, LocalDateTime?>.ne(value: LocalDateTime?) = _ne(this, value)
    infix fun KProperty1<T, LocalDate?>.ne(value: LocalDate?) = _ne(this, value)
    infix fun KProperty1<T, LocalTime?>.ne(value: LocalTime?) = _ne(this, value)
    infix fun KProperty1<T, Long?>.ne(value: Long?) = _ne(this, value)
    infix fun KProperty1<T, NClob?>.ne(value: NClob?) = _ne(this, value)
    infix fun KProperty1<T, Short?>.ne(value: Short?) = _ne(this, value)
    infix fun KProperty1<T, String?>.ne(value: String?) = _ne(this, value)
    infix fun KProperty1<T, SQLXML?>.ne(value: SQLXML?) = _ne(this, value)
    private fun _ne(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Ne(prop, value))
    }

    infix fun KProperty1<T, Any?>.gt(value: () -> Any?) = _gt(this, value())
    infix fun KProperty1<T, java.sql.Array?>.gt(value: java.sql.Array?) = _gt(this, value)
    infix fun KProperty1<T, BigDecimal?>.gt(value: BigDecimal?) = _gt(this, value)
    infix fun KProperty1<T, BigInteger?>.gt(value: BigInteger?) = _gt(this, value)
    infix fun KProperty1<T, Blob?>.gt(value: Blob?) = _gt(this, value)
    infix fun KProperty1<T, Boolean?>.gt(value: Boolean?) = _gt(this, value)
    infix fun KProperty1<T, Byte?>.gt(value: Byte?) = _gt(this, value)
    infix fun KProperty1<T, ByteArray?>.gt(value: ByteArray?) = _gt(this, value)
    infix fun KProperty1<T, Clob?>.gt(value: Clob?) = _gt(this, value)
    infix fun KProperty1<T, Double?>.gt(value: Double?) = _gt(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.gt(value: V?) = _gt(this, value)
    infix fun KProperty1<T, Float?>.gt(value: Float?) = _gt(this, value)
    infix fun KProperty1<T, Int?>.gt(value: Int?) = _gt(this, value)
    infix fun KProperty1<T, LocalDateTime?>.gt(value: LocalDateTime?) = _gt(this, value)
    infix fun KProperty1<T, LocalDate?>.gt(value: LocalDate?) = _gt(this, value)
    infix fun KProperty1<T, LocalTime?>.gt(value: LocalTime?) = _gt(this, value)
    infix fun KProperty1<T, Long?>.gt(value: Long?) = _gt(this, value)
    infix fun KProperty1<T, NClob?>.gt(value: NClob?) = _gt(this, value)
    infix fun KProperty1<T, Short?>.gt(value: Short?) = _gt(this, value)
    infix fun KProperty1<T, String?>.gt(value: String?) = _gt(this, value)
    infix fun KProperty1<T, SQLXML?>.gt(value: SQLXML?) = _gt(this, value)
    private fun _gt(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Gt(prop, value))
    }

    infix fun KProperty1<T, Any?>.ge(value: () -> Any?) = _ge(this, value())
    infix fun KProperty1<T, java.sql.Array?>.ge(value: java.sql.Array?) = _ge(this, value)
    infix fun KProperty1<T, BigDecimal?>.ge(value: BigDecimal?) = _ge(this, value)
    infix fun KProperty1<T, BigInteger?>.ge(value: BigInteger?) = _ge(this, value)
    infix fun KProperty1<T, Blob?>.ge(value: Blob?) = _ge(this, value)
    infix fun KProperty1<T, Boolean?>.ge(value: Boolean?) = _ge(this, value)
    infix fun KProperty1<T, Byte?>.ge(value: Byte?) = _ge(this, value)
    infix fun KProperty1<T, ByteArray?>.ge(value: ByteArray?) = _ge(this, value)
    infix fun KProperty1<T, Clob?>.ge(value: Clob?) = _ge(this, value)
    infix fun KProperty1<T, Double?>.ge(value: Double?) = _ge(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.ge(value: V?) = _ge(this, value)
    infix fun KProperty1<T, Float?>.ge(value: Float?) = _ge(this, value)
    infix fun KProperty1<T, Int?>.ge(value: Int?) = _ge(this, value)
    infix fun KProperty1<T, LocalDateTime?>.ge(value: LocalDateTime?) = _ge(this, value)
    infix fun KProperty1<T, LocalDate?>.ge(value: LocalDate?) = _ge(this, value)
    infix fun KProperty1<T, LocalTime?>.ge(value: LocalTime?) = _ge(this, value)
    infix fun KProperty1<T, Long?>.ge(value: Long?) = _ge(this, value)
    infix fun KProperty1<T, NClob?>.ge(value: NClob?) = _ge(this, value)
    infix fun KProperty1<T, Short?>.ge(value: Short?) = _ge(this, value)
    infix fun KProperty1<T, String?>.ge(value: String?) = _ge(this, value)
    infix fun KProperty1<T, SQLXML?>.ge(value: SQLXML?) = _ge(this, value)
    private fun _ge(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Ge(prop, value))
    }

    infix fun KProperty1<T, Any?>.lt(value: () -> Any?) = _lt(this, value())
    infix fun KProperty1<T, java.sql.Array?>.lt(value: java.sql.Array?) = _lt(this, value)
    infix fun KProperty1<T, BigDecimal?>.lt(value: BigDecimal?) = _lt(this, value)
    infix fun KProperty1<T, BigInteger?>.lt(value: BigInteger?) = _lt(this, value)
    infix fun KProperty1<T, Blob?>.lt(value: Blob?) = _lt(this, value)
    infix fun KProperty1<T, Boolean?>.lt(value: Boolean?) = _lt(this, value)
    infix fun KProperty1<T, Byte?>.lt(value: Byte?) = _lt(this, value)
    infix fun KProperty1<T, ByteArray?>.lt(value: ByteArray?) = _lt(this, value)
    infix fun KProperty1<T, Clob?>.lt(value: Clob?) = _lt(this, value)
    infix fun KProperty1<T, Double?>.lt(value: Double?) = _lt(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.lt(value: V?) = _lt(this, value)
    infix fun KProperty1<T, Float?>.lt(value: Float?) = _lt(this, value)
    infix fun KProperty1<T, Int?>.lt(value: Int?) = _lt(this, value)
    infix fun KProperty1<T, LocalDateTime?>.lt(value: LocalDateTime?) = _lt(this, value)
    infix fun KProperty1<T, LocalDate?>.lt(value: LocalDate?) = _lt(this, value)
    infix fun KProperty1<T, LocalTime?>.lt(value: LocalTime?) = _lt(this, value)
    infix fun KProperty1<T, Long?>.lt(value: Long?) = _lt(this, value)
    infix fun KProperty1<T, NClob?>.lt(value: NClob?) = _lt(this, value)
    infix fun KProperty1<T, Short?>.lt(value: Short?) = _lt(this, value)
    infix fun KProperty1<T, String?>.lt(value: String?) = _lt(this, value)
    infix fun KProperty1<T, SQLXML?>.lt(value: SQLXML?) = _lt(this, value)
    private fun _lt(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Lt(prop, value))
    }

    infix fun KProperty1<T, Any?>.le(value: () -> Any?) = _le(this, value())
    infix fun KProperty1<T, java.sql.Array?>.le(value: java.sql.Array?) = _le(this, value)
    infix fun KProperty1<T, BigDecimal?>.le(value: BigDecimal?) = _le(this, value)
    infix fun KProperty1<T, BigInteger?>.le(value: BigInteger?) = _le(this, value)
    infix fun KProperty1<T, Blob?>.le(value: Blob?) = _le(this, value)
    infix fun KProperty1<T, Boolean?>.le(value: Boolean?) = _le(this, value)
    infix fun KProperty1<T, Byte?>.le(value: Byte?) = _le(this, value)
    infix fun KProperty1<T, ByteArray?>.le(value: ByteArray?) = _le(this, value)
    infix fun KProperty1<T, Clob?>.le(value: Clob?) = _le(this, value)
    infix fun KProperty1<T, Double?>.le(value: Double?) = _le(this, value)
    infix fun <V : Enum<V>> KProperty1<*, V?>.le(value: V?) = _le(this, value)
    infix fun KProperty1<T, Float?>.le(value: Float?) = _le(this, value)
    infix fun KProperty1<T, Int?>.le(value: Int?) = _le(this, value)
    infix fun KProperty1<T, LocalDateTime?>.le(value: LocalDateTime?) = _le(this, value)
    infix fun KProperty1<T, LocalDate?>.le(value: LocalDate?) = _le(this, value)
    infix fun KProperty1<T, LocalTime?>.le(value: LocalTime?) = _le(this, value)
    infix fun KProperty1<T, Long?>.le(value: Long?) = _le(this, value)
    infix fun KProperty1<T, NClob?>.le(value: NClob?) = _le(this, value)
    infix fun KProperty1<T, Short?>.le(value: Short?) = _le(this, value)
    infix fun KProperty1<T, String?>.le(value: String?) = _le(this, value)
    infix fun KProperty1<T, SQLXML?>.le(value: SQLXML?) = _le(this, value)
    private fun _le(prop: KProperty1<*, *>, value: Any?) {
        criterionList.add(Criterion.Le(prop, value))
    }

    infix fun KProperty1<T, String?>.like(value: String?) {
        criterionList.add(Criterion.Like(this, value))
    }

    infix fun KProperty1<T, String?>.notLike(value: String?) {
        criterionList.add(Criterion.NotLike(this, value))
    }

    // in operator
    operator fun <V> Iterable<V>.contains(prop: KProperty1<T, V>): Boolean {
        return criterionList.add(Criterion.In(prop, this))
    }

    fun and(block: WhereScope<T>.() -> Unit) {
        val scope = WhereScope<T>()
        block(scope)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.And(scope.criterionList))
        }
    }

    fun or(block: WhereScope<T>.() -> Unit) {
        val scope = WhereScope<T>()
        block(scope)
        if (scope.criterionList.isNotEmpty()) {
            criterionList.add(Criterion.Or(scope.criterionList))
        }
    }
}
