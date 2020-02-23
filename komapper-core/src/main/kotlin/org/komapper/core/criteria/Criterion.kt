package org.komapper.core.criteria

sealed class Criterion {
    data class Eq(val prop: Expression, val value: Expression) : Criterion()
    data class Ne(val prop: Expression, val value: Expression) : Criterion()
    data class Gt(val prop: Expression, val value: Expression) : Criterion()
    data class Ge(val prop: Expression, val value: Expression) : Criterion()
    data class Lt(val prop: Expression, val value: Expression) : Criterion()
    data class Le(val prop: Expression, val value: Expression) : Criterion()
    data class In(val prop: Expression, val values: List<Expression>) : Criterion()
    data class NotIn(val prop: Expression, val values: List<Expression>) : Criterion()
    data class In2(val prop1: Expression, val prop2: Expression, val values: List<Pair<Expression, Expression>>) :
        Criterion()

    data class NotIn2(val prop1: Expression, val prop2: Expression, val values: List<Pair<Expression, Expression>>) :
        Criterion()

    data class In3(
        val prop1: Expression,
        val prop2: Expression,
        val prop3: Expression,
        val values: List<Triple<Expression, Expression, Expression>>
    ) : Criterion()

    data class NotIn3(
        val prop1: Expression,
        val prop2: Expression,
        val prop3: Expression,
        val values: List<Triple<Expression, Expression, Expression>>
    ) : Criterion()

    data class Like(val prop: Expression, val value: Expression) : Criterion()
    data class NotLike(val prop: Expression, val value: Expression) : Criterion()
    data class Between(val prop: Expression, val range: Pair<Expression, Expression>) : Criterion()
    data class Exists(val criteria: SelectCriteria<*>) : Criterion()
    data class NotExists(val criteria: SelectCriteria<*>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
}
