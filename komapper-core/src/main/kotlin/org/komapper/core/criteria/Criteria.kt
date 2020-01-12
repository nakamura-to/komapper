package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class Criteria<T : Any>(
    val kClass: KClass<T>,
    val joins: List<Join>,
    val where: List<Criterion>,
    val orderBy: List<Pair<KProperty1<*, *>, String>>,
    val limit: Int?,
    val offset: Int?,
    val forUpdate: ForUpdate?
)

data class MutableCriteria<T : Any>(
    val kClass: KClass<T>,
    val joins: MutableList<Join> = mutableListOf(),
    val where: MutableList<Criterion> = mutableListOf(),
    val orderBy: MutableList<Pair<KProperty1<*, *>, String>> = mutableListOf(),
    var limit: Int? = null,
    var offset: Int? = null,
    var forUpdate: ForUpdate? = null
) {
    fun asImmutable(): Criteria<T> {
        return Criteria(kClass, joins, where, orderBy, limit, offset, forUpdate)
    }
}

data class Join(
    val kind: JoinKind,
    val type: KClass<*>,
    val on: List<Criterion>,
    val block: (Any, Any) -> Unit
)

enum class JoinKind {
    INNER,
    LEFT
}

sealed class Criterion {
    data class Eq(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ne(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Gt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ge(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Lt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Le(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class In(val prop: KProperty1<*, *>, val values: List<*>) : Criterion()
    data class NotIn(val prop: KProperty1<*, *>, val values: List<*>) : Criterion()
    data class In2(val props: Pair<KProperty1<*, *>, KProperty1<*, *>>, val values: List<Pair<*, *>>) : Criterion()
    data class NotIn2(val props: Pair<KProperty1<*, *>, KProperty1<*, *>>, val values: List<Pair<*, *>>) :
        Criterion()

    data class In3(
        val props: Triple<KProperty1<*, *>, KProperty1<*, *>, KProperty1<*, *>>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class NotIn3(
        val props: Triple<KProperty1<*, *>, KProperty1<*, *>, KProperty1<*, *>>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class Like(val prop: KProperty1<*, *>, val value: String?) : Criterion()
    data class NotLike(val prop: KProperty1<*, *>, val value: String?) : Criterion()
    data class Between(val prop: KProperty1<*, *>, val range: Pair<*, *>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
}

data class ForUpdate(val nowait: Boolean = false)

typealias CriteriaQuery<T> = SelectScope<T>.() -> Unit

fun <T : Any> select(criteriaQuery: CriteriaQuery<T>): CriteriaQuery<T> = criteriaQuery

infix operator fun <T : Any> (CriteriaQuery<T>).plus(other: CriteriaQuery<T>): CriteriaQuery<T> {
    val self = this
    return {
        self()
        other()
    }
}
