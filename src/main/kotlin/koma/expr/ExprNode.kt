package koma.expr

import kotlin.reflect.KClass

sealed class ExprNode {
    abstract val location: ExprLocation
}

sealed class UnaryOp : ExprNode() {
    abstract val operand: ExprNode
}

sealed class BinaryOp : ExprNode() {
    abstract val left: ExprNode
    abstract val right: ExprNode
}

data class Not(override val location: ExprLocation, override val operand: ExprNode) : UnaryOp()
data class Or(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class And(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Eq(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Ne(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Ge(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Le(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Gt(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Lt(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class Property(override val location: ExprLocation, val name: String, val receiver: ExprNode) : ExprNode()
data class Function(override val location: ExprLocation, val name: String, val receiver: ExprNode, val args: ExprNode) :
    ExprNode()

data class Comma(override val location: ExprLocation, val nodeList: List<ExprNode>) : ExprNode()
data class Value(override val location: ExprLocation, val name: String) : ExprNode()
data class Literal(override val location: ExprLocation, val value: Any?, val kClass: KClass<out Any>) : ExprNode()
data class Empty(override val location: ExprLocation) : ExprNode()
