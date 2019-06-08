package org.komapper.criteria

import kotlin.reflect.KProperty1

sealed class Criterion {
    data class Eq(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ne(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Gt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ge(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Lt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Le(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class In(val prop: KProperty1<*, *>, val values: List<*>) : Criterion()
    data class NotIn(val prop: KProperty1<*, *>, val values: List<*>) : Criterion()
    data class Like(val prop: KProperty1<*, *>, val value: String?) : Criterion()
    data class NotLike(val prop: KProperty1<*, *>, val value: String?) : Criterion()
    data class Between(val prop: KProperty1<*, *>, val range: Pair<*, *>) : Criterion()
    data class Not(val criterionList: List<Criterion>) : Criterion()
    data class And(val criterionList: List<Criterion>) : Criterion()
    data class Or(val criterionList: List<Criterion>) : Criterion()
}
