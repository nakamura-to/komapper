package org.komapper.expr

import java.util.*

abstract class ExprReducer(val priority: Int, val location: ExprLocation) {
    abstract fun reduce(deque: Deque<ExprNode>): ExprNode

    fun pop(deque: Deque<ExprNode>): ExprNode {
        return deque.poll() ?: throw ExprException("The operand is not found at $location")
    }
}

class PropertyReducer(location: ExprLocation, val name: String) :
    org.komapper.expr.ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val receiver = pop(deque)
        return PropertyNode(location, name, receiver)
    }
}

class FunctionReducer(location: ExprLocation, val name: String) :
    org.komapper.expr.ExprReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val args = pop(deque)
        val receiver = pop(deque)
        return FunctionNode(location, name, receiver, args)
    }
}

class NotReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(50, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val expr = pop(deque)
        return NotNode(location, expr)
    }
}

class EqReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return EqNode(location, left, right)
    }
}

class NeReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return NeNode(location, left, right)
    }
}

class GeReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return GeNode(location, left, right)
    }
}

class GtReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return GtNode(location, left, right)
    }
}

class LeReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return LeNode(location, left, right)
    }
}

class LtReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return LtNode(location, left, right)
    }
}

class AndReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(20, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return AndNode(location, left, right)
    }
}

class OrReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(10, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return OrNode(location, left, right)
    }
}

class CommaReducer(location: ExprLocation) : org.komapper.expr.ExprReducer(0, location) {
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
