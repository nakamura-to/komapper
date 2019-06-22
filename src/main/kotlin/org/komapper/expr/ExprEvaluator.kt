package org.komapper.expr

import org.komapper.Value
import kotlin.reflect.KCallable
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

interface ExprEvaluator {
    fun eval(expression: String, ctx: Map<String, Value> = emptyMap()): Value
}

class DefaultExprEvaluator(
    private val exprNodeFactory: ExprNodeFactory,
    private val extensions: List<KCallable<Any?>> = emptyList()
) : ExprEvaluator {

    init {
        extensions.forEach { it.isAccessible = true }
    }

    // used to distinguish multiple arguments from a single List
    class ArgList : ArrayList<Any?>()

    override fun eval(expression: String, ctx: Map<String, Value>): Value {
        val node = exprNodeFactory.get(expression)
        return visit(node, ctx)
    }

    private fun visit(node: ExprNode, ctx: Map<String, Value>): Value = when (node) {
        is ExprNode.Not -> perform(node.location, node.operand, ctx) { !it }
        is ExprNode.And -> perform(node.location, node.left, node.right, ctx) { x, y -> x && y }
        is ExprNode.Or -> perform(node.location, node.left, node.right, ctx) { x, y -> x || y }
        is ExprNode.Eq -> equal(node.location, node.left, node.right, ctx) { x, y -> x == y }
        is ExprNode.Ne -> equal(node.location, node.left, node.right, ctx) { x, y -> x != y }
        is ExprNode.Ge -> compare(node.location, node.left, node.right, ctx) { x, y -> x >= y }
        is ExprNode.Gt -> compare(node.location, node.left, node.right, ctx) { x, y -> x > y }
        is ExprNode.Le -> compare(node.location, node.left, node.right, ctx) { x, y -> x <= y }
        is ExprNode.Lt -> compare(node.location, node.left, node.right, ctx) { x, y -> x < y }
        is ExprNode.Literal -> node.value to node.kClass
        is ExprNode.Comma -> node.nodeList.map {
            visit(it, ctx)
        }.map { it.first }.toCollection(ArgList()) to List::class
        is ExprNode.Value -> visitValue(node, ctx)
        is ExprNode.Property -> visitProperty(node, ctx)
        is ExprNode.Function -> visitFunction(node, ctx)
        is ExprNode.Empty -> Unit to Unit::class
    }

    private fun perform(
        location: ExprLocation,
        operand: ExprNode,
        ctx: Map<String, Value>,
        f: (Boolean) -> Boolean
    ): Value {
        fun checkNull(location: ExprLocation, value: Any?) {
            if (value != null) {
                return
            }
            throw ExprException(
                "Cannot perform the logical operator because the operand is null at $location"
            )
        }

        val (value) = visit(operand, ctx)
        checkNull(operand.location, value)
        if (value !is Boolean) {
            throw ExprException(
                "Cannot perform the logical operator because the operands is not Boolean at $location"
            )
        }
        return f(value) to Boolean::class
    }

    private fun perform(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        ctx: Map<String, Value>,
        f: (Boolean, Boolean) -> Boolean
    ): Value {
        fun checkNull(location: ExprLocation, value: Any?, which: String) {
            if (value != null) {
                return
            }
            throw ExprException(
                "Cannot perform the logical operator because the $which operand is null at $location"
            )
        }

        val (left) = visit(leftNode, ctx)
        val (right) = visit(rightNode, ctx)
        checkNull(leftNode.location, left, "left")
        checkNull(rightNode.location, right, "right")
        if (left !is Boolean || right !is Boolean) {
            throw ExprException(
                "Cannot perform the logical operator because either operands is not Boolean at $location"
            )
        }
        return f(left, right) to Boolean::class
    }

    @Suppress("UNUSED_PARAMETER")
    private fun equal(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        ctx: Map<String, Value>,
        f: (Any?, Any?) -> Boolean
    ): Value {
        val (left) = visit(leftNode, ctx)
        val (right) = visit(rightNode, ctx)
        return f(left, right) to Boolean::class
    }

    @Suppress("UNCHECKED_CAST")
    private fun compare(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        ctx: Map<String, Value>,
        f: (Comparable<Any>, Comparable<Any>) -> Boolean
    ): Value {
        fun checkNull(location: ExprLocation, value: Any?, which: String) {
            if (value != null) {
                return
            }
            throw ExprException(
                "Cannot compare because the $which operand is null at $location"
            )
        }

        val (left) = visit(leftNode, ctx)
        val (right) = visit(rightNode, ctx)
        checkNull(leftNode.location, left, "left")
        checkNull(rightNode.location, right, "right")
        try {
            left as Comparable<Any>
            right as Comparable<Any>
            return f(left, right) to Boolean::class
        } catch (e: ClassCastException) {
            throw ExprException(
                "Cannot compare because the operands are not comparable to each other at $location"
            )
        }
    }

    private fun visitValue(node: ExprNode.Value, ctx: Map<String, Value>): Value {
        return ctx[node.name] ?: null to Any::class
    }

    private fun visitProperty(node: ExprNode.Property, ctx: Map<String, Value>): Value {
        val (receiver) = visit(node.receiver, ctx)
        val property = findProperty(node.name, receiver)
            ?: throw ExprException("The receiver of the property \"${node.name}\" is null or the property is not found at ${node.location}")
        try {
            return property.call(receiver) to property.returnType.jvmErasure
        } catch (cause: Exception) {
            throw ExprException("Failed to call the property \"${node.name}\" at ${node.location}. The cause is $cause")
        }
    }

    private fun findProperty(name: String, receiver: Any?): KCallable<*>? {
        fun predicate(callable: KCallable<*>): Boolean {
            return name == callable.name && callable.valueParameters.isEmpty()
        }

        if (receiver != null) {
            val property = receiver::class.memberProperties.find(::predicate)
            if (property != null) {
                return property
            }
        }
        return extensions.find(::predicate)
    }

    private fun visitFunction(node: ExprNode.Function, ctx: Map<String, Value>): Value {
        val (receiver) = visit(node.receiver, ctx)
        // arguments for KCallable
        val (args) = visit(node.args, ctx)
        val arguments = when (args) {
            Unit -> listOf(receiver)
            is ArgList -> listOf(receiver) + args
            else -> listOf(receiver, args)
        }
        val function = findFunction(node.name, receiver, arguments)
            ?: throw ExprException("The receiver of the function \"${node.name}\" is null or the function is not found at ${node.location}")
        try {
            return function.call(*arguments.toTypedArray()) to function.returnType.jvmErasure
        } catch (cause: Exception) {
            throw ExprException("Failed to call the function \"${node.name}\" at ${node.location}. The cause is $cause")
        }
    }

    private fun findFunction(name: String, receiver: Any?, args: List<*>): KCallable<*>? {
        fun predicate(callable: KCallable<*>): Boolean {
            return if (name == callable.name && args.size == callable.parameters.size) {
                args.zip(callable.parameters).all { (arg, param) ->
                    arg == null || arg::class.isSubclassOf(param.type.jvmErasure)
                }
            } else false
        }

        if (receiver != null) {
            val function = receiver::class.memberFunctions.find(::predicate)
            if (function != null) {
                return function
            }
        }
        return extensions.find(::predicate)
    }
}
