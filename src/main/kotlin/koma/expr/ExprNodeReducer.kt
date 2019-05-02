package koma.expr

import java.util.*

abstract class ExprNodeReducer(val priority: Int, val location: ExprLocation) {
    abstract fun reduce(deque: Deque<ExprNode>): ExprNode

    fun pop(deque: Deque<ExprNode>): ExprNode {
        return deque.poll() ?: throw ExprException("The operand is not found at $location")
    }
}

class PropertyReducer(location: ExprLocation, val name: String) : ExprNodeReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val receiver = pop(deque)
        return Property(location, name, receiver)
    }
}

class FunctionReducer(location: ExprLocation, val name: String) : ExprNodeReducer(100, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val args = pop(deque)
        val receiver = pop(deque)
        return Function(location, name, receiver, args)
    }
}

class NotReducer(location: ExprLocation) : ExprNodeReducer(50, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val expr = pop(deque)
        return Not(location, expr)
    }
}

class EqReducer(location: ExprLocation) : ExprNodeReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Eq(location, left, right)
    }
}

class NeReducer(location: ExprLocation) : ExprNodeReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Ne(location, left, right)
    }
}

class GeReducer(location: ExprLocation) : ExprNodeReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Ge(location, left, right)
    }
}

class GtReducer(location: ExprLocation) : ExprNodeReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Gt(location, left, right)
    }
}

class LeReducer(location: ExprLocation) : ExprNodeReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Le(location, left, right)
    }
}

class LtReducer(location: ExprLocation) : ExprNodeReducer(40, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Lt(location, left, right)
    }
}

class AndReducer(location: ExprLocation) : ExprNodeReducer(20, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return And(location, left, right)
    }
}

class OrReducer(location: ExprLocation) : ExprNodeReducer(10, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        return Or(location, left, right)
    }
}

class CommaReducer(location: ExprLocation) : ExprNodeReducer(0, location) {
    override fun reduce(deque: Deque<ExprNode>): ExprNode {
        val right = pop(deque)
        val left = pop(deque)
        val exprList = when (right) {
            is Comma -> listOf(left) + right.nodeList
            else -> listOf(left, right)
        }
        return Comma(location, exprList)
    }
}
