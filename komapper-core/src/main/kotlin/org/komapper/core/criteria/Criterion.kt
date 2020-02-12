package org.komapper.core.criteria

sealed class Criterion {
    data class Eq(val prop: Expr, val value: Expr) : Criterion()
    data class Ne(val prop: Expr, val value: Expr) : Criterion()
    data class Gt(val prop: Expr, val value: Expr) : Criterion()
    data class Ge(val prop: Expr, val value: Expr) : Criterion()
    data class Lt(val prop: Expr, val value: Expr) : Criterion()
    data class Le(val prop: Expr, val value: Expr) : Criterion()
    data class In(val prop: Expr, val values: List<Expr>) : Criterion()
    data class NotIn(val prop: Expr, val values: List<Expr>) : Criterion()
    data class In2(val prop1: Expr, val prop2: Expr, val values: List<Pair<Expr, Expr>>) :
        Criterion()

    data class NotIn2(val prop1: Expr, val prop2: Expr, val values: List<Pair<Expr, Expr>>) :
        Criterion()

    data class In3(
        val prop1: Expr,
        val prop2: Expr,
        val prop3: Expr,
        val values: List<Triple<Expr, Expr, Expr>>
    ) : Criterion()

    data class NotIn3(
        val prop1: Expr,
        val prop2: Expr,
        val prop3: Expr,
        val values: List<Triple<Expr, Expr, Expr>>
    ) : Criterion()

    data class Like(val prop: Expr, val value: Expr) : Criterion()
    data class NotLike(val prop: Expr, val value: Expr) : Criterion()
    data class Between(val prop: Expr, val range: Pair<Expr, Expr>) : Criterion()
    data class Exists(val criteria: SelectCriteria<*>) : Criterion()
    data class NotExists(val criteria: SelectCriteria<*>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
}
