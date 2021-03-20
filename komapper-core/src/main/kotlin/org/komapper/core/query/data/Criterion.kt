package org.komapper.core.query.data

internal sealed class Criterion {
    data class Eq(val left: Operand, val right: Operand) : Criterion()
    data class NotEq(val left: Operand, val right: Operand) : Criterion()
    data class Less(val left: Operand, val right: Operand) : Criterion()
    data class LessEq(val left: Operand, val right: Operand) : Criterion()
    data class Grater(val left: Operand, val right: Operand) : Criterion()
    data class GraterEq(val left: Operand, val right: Operand) : Criterion()
    data class InList(val left: Operand, val right: List<Operand>) : Criterion()
    data class NotInList(val left: Operand, val right: List<Operand>) : Criterion()

    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
}
