package org.komapper.core.criteria

sealed class Criterion {
    data class Eq(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Ne(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Gt(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Ge(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Lt(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Le(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class In(val prop: AliasProperty<*, *>, val values: List<*>) : Criterion()
    data class NotIn(val prop: AliasProperty<*, *>, val values: List<*>) : Criterion()
    data class In2(val prop1: AliasProperty<*, *>, val prop2: AliasProperty<*, *>, val values: List<Pair<*, *>>) :
        Criterion()

    data class NotIn2(val prop1: AliasProperty<*, *>, val prop2: AliasProperty<*, *>, val values: List<Pair<*, *>>) :
        Criterion()

    data class In3(
        val prop1: AliasProperty<*, *>,
        val prop2: AliasProperty<*, *>,
        val prop3: AliasProperty<*, *>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class NotIn3(
        val prop1: AliasProperty<*, *>,
        val prop2: AliasProperty<*, *>,
        val prop3: AliasProperty<*, *>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class Like(val prop: AliasProperty<*, *>, val value: String?) : Criterion()
    data class NotLike(val prop: AliasProperty<*, *>, val value: String?) : Criterion()
    data class Between(val prop: AliasProperty<*, *>, val range: Pair<*, *>) : Criterion()
    data class Exists(val criteria: SelectCriteria<*>) : Criterion()
    data class NotExists(val criteria: SelectCriteria<*>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
}
