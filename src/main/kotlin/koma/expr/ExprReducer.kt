package koma.expr

import java.util.*

abstract class ExprReducer(val priority: Int, val location: ExprLocation) {
    abstract fun reduce(deque: Deque<ExprNode>): ExprNode

    fun pop(deque: Deque<ExprNode>): ExprNode {
        return deque.poll() ?: throw ExprException("The operand is not found at $location")
    }
}

class PropertyReducer(location: ExprLocation, val name: String) : ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val receiver = pop(deque)
        return PropertyNode(location, name, receiver)
    }
}

class FunctionReducer(location: ExprLocation, val name: String) : ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val args = pop(deque)
        val receiver = pop(deque)
        return FunctionNode(location, name, receiver, args)
    }
}

class NotReducer(location: ExprLocation) : ExprReducer(50, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val expr = pop(deque)
        return NotNode(location, expr)
    }
}

class EqReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return EqNode(location, left, right)
    }
}

class NeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return NeNode(location, left, right)
    }
}

class GeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return GeNode(location, left, right)
    }
}

class GtReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return GtNode(location, left, right)
    }
}

class LeReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return LeNode(location, left, right)
    }
}

class LtReducer(location: ExprLocation) : ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return LtNode(location, left, right)
    }
}

class AndReducer(location: ExprLocation) : ExprReducer(20, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return AndNode(location, left, right)
    }
}

class OrReducer(location: ExprLocation) : ExprReducer(10, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return OrNode(location, left, right)
    }
}

class CommaReducer(location: ExprLocation) : ExprReducer(0, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        val exprList = when (right) {
            is CommaNode -> listOf(left) + right.nodeList
            else -> listOf(left, right)
        }
        return CommaNode(location, exprList)
    }
}
