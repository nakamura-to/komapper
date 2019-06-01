package koma.criteria

import kotlin.reflect.KProperty1

sealed class Criterion {
    data class Eq(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ne(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Gt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ge(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Lt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Le(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class And(val criterionList: List<Criterion>) : Criterion()
    data class Or(val criterionList: List<Criterion>) : Criterion()
    data class In(val prop: KProperty1<*, *>, val values: Iterable<*>) : Criterion()
}
