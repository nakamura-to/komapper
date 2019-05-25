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

data class NotNode(override val location: ExprLocation, override val operand: ExprNode) : UnaryOp()
data class OrNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class AndNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class EqNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class NeNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class GeNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class LeNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class GtNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class LtNode(override val location: ExprLocation, override val left: ExprNode, override val right: ExprNode) :
    BinaryOp()

data class PropertyNode(override val location: ExprLocation, val name: String, val receiver: ExprNode) : ExprNode()
data class FunctionNode(
    override val location: ExprLocation,
    val name: String,
    val receiver: ExprNode,
    val args: ExprNode
) :
    ExprNode()

data class CommaNode(override val location: ExprLocation, val nodeList: List<ExprNode>) : ExprNode()
data class ValueNode(override val location: ExprLocation, val name: String) : ExprNode()
data class LiteralNode(override val location: ExprLocation, val value: Any?, val kClass: KClass<out Any>) : ExprNode()
data class EmptyNode(override val location: ExprLocation) : ExprNode()
