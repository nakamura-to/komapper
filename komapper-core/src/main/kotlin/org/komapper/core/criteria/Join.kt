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
import org.komapper.core.dsl.EmptyScope
import org.komapper.core.dsl.Scope

typealias Join<T, S> = JoinScope<T, S>.() -> Unit

fun <T : Any, S : Any> join(block: Join<T, S>) = block

infix operator fun <T : Any, S : Any> (Join<T, S>).plus(other: Join<T, S>): Join<T, S> {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class JoinScope<T : Any, S : Any>(
    val _add: (Criterion) -> Unit,
    val _associate: (EmptyScope.(T, S) -> Unit) -> Unit
) {

    fun eqAny(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Array?>,
        p2: KProperty1<S, Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, BigDecimal?>,
        p2: KProperty1<S, BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, BigInteger?>,
        p2: KProperty1<S, BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Blob?>,
        p2: KProperty1<S, Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Boolean?>,
        p2: KProperty1<S, Boolean?>,
        @Suppress("UNUSED_PARAMETER") `_`: Boolean? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Byte?>,
        p2: KProperty1<S, Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, ByteArray?>,
        p2: KProperty1<S, ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Clob?>,
        p2: KProperty1<S, Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Double?>,
        p2: KProperty1<S, Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _eq(p1, p2)

    fun <V : Enum<V>> eq(
        p1: KProperty1<T, V?>,
        p2: KProperty1<S, V?>,
        @Suppress("UNUSED_PARAMETER") `_`: V? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Float?>,
        p2: KProperty1<S, Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Int?>,
        p2: KProperty1<S, Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, LocalDateTime?>,
        p2: KProperty1<S, LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, LocalDate?>,
        p2: KProperty1<S, LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, LocalTime?>,
        p2: KProperty1<S, LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Long?>,
        p2: KProperty1<S, Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, NClob?>,
        p2: KProperty1<S, NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, Short?>,
        p2: KProperty1<S, Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, String?>,
        p2: KProperty1<S, String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _eq(p1, p2)

    fun eq(
        p1: KProperty1<T, SQLXML?>,
        p2: KProperty1<S, SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _eq(p1, p2)

    fun _eq(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _add(Criterion.Eq(p1, p2))

    fun neAny(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Array?>,
        p2: KProperty1<S, Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, BigDecimal?>,
        p2: KProperty1<S, BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, BigInteger?>,
        p2: KProperty1<S, BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Blob?>,
        p2: KProperty1<S, Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Boolean?>,
        p2: KProperty1<S, Boolean?>,
        @Suppress("UNUSED_PARAMETER") `_`: Boolean? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Byte?>,
        p2: KProperty1<S, Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, ByteArray?>,
        p2: KProperty1<S, ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Clob?>,
        p2: KProperty1<S, Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Double?>,
        p2: KProperty1<S, Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _ne(p1, p2)

    fun <V : Enum<V>> ne(
        p1: KProperty1<T, V?>,
        p2: KProperty1<S, V?>,
        @Suppress("UNUSED_PARAMETER") `_`: V? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Float?>,
        p2: KProperty1<S, Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Int?>,
        p2: KProperty1<S, Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, LocalDateTime?>,
        p2: KProperty1<S, LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, LocalDate?>,
        p2: KProperty1<S, LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, LocalTime?>,
        p2: KProperty1<S, LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Long?>,
        p2: KProperty1<S, Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, NClob?>,
        p2: KProperty1<S, NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, Short?>,
        p2: KProperty1<S, Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, String?>,
        p2: KProperty1<S, String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _ne(p1, p2)

    fun ne(
        p1: KProperty1<T, SQLXML?>,
        p2: KProperty1<S, SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _ne(p1, p2)

    fun _ne(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _add(Criterion.Ne(p1, p2))

    fun gtAny(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Array?>,
        p2: KProperty1<S, Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, BigDecimal?>,
        p2: KProperty1<S, BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, BigInteger?>,
        p2: KProperty1<S, BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Blob?>,
        p2: KProperty1<S, Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Boolean?>,
        p2: KProperty1<S, Boolean?>,
        @Suppress("UNUSED_PARAMETER") `_`: Boolean? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Byte?>,
        p2: KProperty1<S, Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, ByteArray?>,
        p2: KProperty1<S, ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Clob?>,
        p2: KProperty1<S, Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Double?>,
        p2: KProperty1<S, Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _gt(p1, p2)

    fun <V : Enum<V>> gt(
        p1: KProperty1<T, V?>,
        p2: KProperty1<S, V?>,
        @Suppress("UNUSED_PARAMETER") `_`: V? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Float?>,
        p2: KProperty1<S, Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Int?>,
        p2: KProperty1<S, Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, LocalDateTime?>,
        p2: KProperty1<S, LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, LocalDate?>,
        p2: KProperty1<S, LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, LocalTime?>,
        p2: KProperty1<S, LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Long?>,
        p2: KProperty1<S, Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, NClob?>,
        p2: KProperty1<S, NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, Short?>,
        p2: KProperty1<S, Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, String?>,
        p2: KProperty1<S, String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _gt(p1, p2)

    fun gt(
        p1: KProperty1<T, SQLXML?>,
        p2: KProperty1<S, SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _gt(p1, p2)

    fun _gt(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _add(Criterion.Gt(p1, p2))

    fun geAny(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Array?>,
        p2: KProperty1<S, Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, BigDecimal?>,
        p2: KProperty1<S, BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, BigInteger?>,
        p2: KProperty1<S, BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Blob?>,
        p2: KProperty1<S, Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Boolean?>,
        p2: KProperty1<S, Boolean?>,
        @Suppress("UNUSED_PARAMETER") `_`: Boolean? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Byte?>,
        p2: KProperty1<S, Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, ByteArray?>,
        p2: KProperty1<S, ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Clob?>,
        p2: KProperty1<S, Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Double?>,
        p2: KProperty1<S, Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _ge(p1, p2)

    fun <V : Enum<V>> ge(
        p1: KProperty1<T, V?>,
        p2: KProperty1<S, V?>,
        @Suppress("UNUSED_PARAMETER") `_`: V? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Float?>,
        p2: KProperty1<S, Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Int?>,
        p2: KProperty1<S, Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, LocalDateTime?>,
        p2: KProperty1<S, LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, LocalDate?>,
        p2: KProperty1<S, LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, LocalTime?>,
        p2: KProperty1<S, LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Long?>,
        p2: KProperty1<S, Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, NClob?>,
        p2: KProperty1<S, NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, Short?>,
        p2: KProperty1<S, Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, String?>,
        p2: KProperty1<S, String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _ge(p1, p2)

    fun ge(
        p1: KProperty1<T, SQLXML?>,
        p2: KProperty1<S, SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _ge(p1, p2)

    fun _ge(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _add(Criterion.Ge(p1, p2))

    fun ltAny(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Array?>,
        p2: KProperty1<S, Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, BigDecimal?>,
        p2: KProperty1<S, BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, BigInteger?>,
        p2: KProperty1<S, BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Blob?>,
        p2: KProperty1<S, Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Boolean?>,
        p2: KProperty1<S, Boolean?>,
        @Suppress("UNUSED_PARAMETER") `_`: Boolean? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Byte?>,
        p2: KProperty1<S, Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, ByteArray?>,
        p2: KProperty1<S, ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Clob?>,
        p2: KProperty1<S, Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Double?>,
        p2: KProperty1<S, Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _lt(p1, p2)

    fun <V : Enum<V>> lt(
        p1: KProperty1<T, V?>,
        p2: KProperty1<S, V?>,
        @Suppress("UNUSED_PARAMETER") `_`: V? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Float?>,
        p2: KProperty1<S, Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Int?>,
        p2: KProperty1<S, Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, LocalDateTime?>,
        p2: KProperty1<S, LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, LocalDate?>,
        p2: KProperty1<S, LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, LocalTime?>,
        p2: KProperty1<S, LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Long?>,
        p2: KProperty1<S, Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, NClob?>,
        p2: KProperty1<S, NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, Short?>,
        p2: KProperty1<S, Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, String?>,
        p2: KProperty1<S, String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _lt(p1, p2)

    fun lt(
        p1: KProperty1<T, SQLXML?>,
        p2: KProperty1<S, SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _lt(p1, p2)

    fun _lt(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _add(Criterion.Lt(p1, p2))

    fun leAny(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Array?>,
        p2: KProperty1<S, Array?>,
        @Suppress("UNUSED_PARAMETER") `_`: Array? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, BigDecimal?>,
        p2: KProperty1<S, BigDecimal?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigDecimal? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, BigInteger?>,
        p2: KProperty1<S, BigInteger?>,
        @Suppress("UNUSED_PARAMETER") `_`: BigInteger? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Blob?>,
        p2: KProperty1<S, Blob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Blob? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Boolean?>,
        p2: KProperty1<S, Boolean?>,
        @Suppress("UNUSED_PARAMETER") `_`: Boolean? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Byte?>,
        p2: KProperty1<S, Byte?>,
        @Suppress("UNUSED_PARAMETER") `_`: Byte? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, ByteArray?>,
        p2: KProperty1<S, ByteArray?>,
        @Suppress("UNUSED_PARAMETER") `_`: ByteArray? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Clob?>,
        p2: KProperty1<S, Clob?>,
        @Suppress("UNUSED_PARAMETER") `_`: Clob? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Double?>,
        p2: KProperty1<S, Double?>,
        @Suppress("UNUSED_PARAMETER") `_`: Double? = null
    ) = _le(p1, p2)

    fun <V : Enum<V>> le(
        p1: KProperty1<T, V?>,
        p2: KProperty1<S, V?>,
        @Suppress("UNUSED_PARAMETER") `_`: V? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Float?>,
        p2: KProperty1<S, Float?>,
        @Suppress("UNUSED_PARAMETER") `_`: Float? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Int?>,
        p2: KProperty1<S, Int?>,
        @Suppress("UNUSED_PARAMETER") `_`: Int? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, LocalDateTime?>,
        p2: KProperty1<S, LocalDateTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDateTime? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, LocalDate?>,
        p2: KProperty1<S, LocalDate?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalDate? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, LocalTime?>,
        p2: KProperty1<S, LocalTime?>,
        @Suppress("UNUSED_PARAMETER") `_`: LocalTime? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Long?>,
        p2: KProperty1<S, Long?>,
        @Suppress("UNUSED_PARAMETER") `_`: Long? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, NClob?>,
        p2: KProperty1<S, NClob?>,
        @Suppress("UNUSED_PARAMETER") `_`: NClob? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, Short?>,
        p2: KProperty1<S, Short?>,
        @Suppress("UNUSED_PARAMETER") `_`: Short? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, String?>,
        p2: KProperty1<S, String?>,
        @Suppress("UNUSED_PARAMETER") `_`: String? = null
    ) = _le(p1, p2)

    fun le(
        p1: KProperty1<T, SQLXML?>,
        p2: KProperty1<S, SQLXML?>,
        @Suppress("UNUSED_PARAMETER") `_`: SQLXML? = null
    ) = _le(p1, p2)

    fun _le(
        p1: KProperty1<T, *>,
        p2: KProperty1<S, *>
    ) = _add(Criterion.Le(p1, p2))

    fun associate(block: EmptyScope.(T, S) -> Unit) {
        _associate(block)
    }
}
