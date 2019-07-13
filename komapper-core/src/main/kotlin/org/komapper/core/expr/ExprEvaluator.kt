package org.komapper.core.expr

import org.komapper.core.value.Value
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

interface ExprEvaluator {
    fun eval(expression: String, ctx: Map<String, Value> = emptyMap()): Value
}

class DefaultExprEvaluator(
    private val exprNodeFactory: ExprNodeFactory,
    private val exprEnvironment: ExprEnvironment
) : ExprEvaluator {

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
        is ExprNode.Literal -> Value(node.value, node.kClass)
        is ExprNode.Comma -> node.nodeList.map {
            visit(it, ctx)
        }.map { it.obj }.toCollection(ArgList()).let {
            Value(
                it,
                List::class
            )
        }
        is ExprNode.Value -> visitValue(node, ctx)
        is ExprNode.Property -> visitProperty(node, ctx)
        is ExprNode.Function -> visitFunction(node, ctx)
        is ExprNode.Empty -> Value(Unit, Unit::class)
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
        return Value(f(value), Boolean::class)
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
        return Value(f(left, right), Boolean::class)
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
        return Value(f(left, right), Boolean::class)
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
            return Value(f(left, right), Boolean::class)
        } catch (e: ClassCastException) {
            throw ExprException(
                "Cannot compare because the operands are not comparable to each other at $location"
            )
        }
    }

    private fun visitValue(node: ExprNode.Value, ctx: Map<String, Value>): Value {
        return ctx[node.name] ?: exprEnvironment.ctx[node.name] ?: Value(null, Any::class)
    }

    private fun visitProperty(node: ExprNode.Property, ctx: Map<String, Value>): Value {
        val (receiver, receiverType) = visit(node.receiver, ctx)
        val property = findProperty(node.name, receiverType)
            ?: throw ExprException("The property \"${node.name}\" is not found at ${node.location}")
        if (receiver == null && node.safeCall) {
            return Value(null, property.returnType)
        }
        try {
            return Value(property.call(receiver), property.returnType)
        } catch (cause: Exception) {
            throw ExprException("Failed to call the property \"${node.name}\" at ${node.location}. The cause is $cause")
        }
    }

    private fun findProperty(name: String, receiverType: KClass<*>): KCallable<*>? {
        fun predicate(callable: KCallable<*>) =
            name == callable.name && callable.valueParameters.isEmpty()
        return receiverType.memberProperties.find(::predicate)
            ?: exprEnvironment.topLevelPropertyExtensions.find(::predicate)
    }

    private fun visitFunction(node: ExprNode.Function, ctx: Map<String, Value>): Value {
        val (receiver, receiverType) = visit(node.receiver, ctx)
        val (args) = visit(node.args, ctx)
        val (function, arguments) = findFunction(node.name, receiverType, receiver, args)
            ?: throw ExprException("The function \"${node.name}\" is not found at ${node.location}")
        if (receiver == null && node.safeCall) {
            return Value(null, function.returnType)
        }
        try {
            return Value(function.call(*arguments.toTypedArray()), function.returnType)
        } catch (cause: Exception) {
            throw ExprException("Failed to call the function \"${node.name}\" at ${node.location}. The cause is $cause")
        }
    }

    private fun findFunction(
        name: String,
        receiverType: KClass<*>,
        receiver: Any?,
        args: Any?
    ): Pair<KCallable<*>, List<Any?>>? {
        fun Collection<KCallable<*>>.pick(arguments: List<Any?>): Pair<KCallable<*>, List<Any?>>? {
            return this.filter { callable ->
                if (name == callable.name && arguments.size == callable.parameters.size) {
                    arguments.zip(callable.parameters).all { (argument, param) ->
                        argument == null || argument::class.isSubclassOf(param.type.jvmErasure)
                    }
                } else false
            }.map { it to arguments }.firstOrNull()
        }

        val arguments = when (args) {
            Unit -> listOf(receiver)
            is ArgList -> listOf(receiver) + args
            else -> listOf(receiver, args)
        }
        return receiverType.memberFunctions.pick(arguments)
            ?: exprEnvironment.topLevelFunctionExtensions.pick(arguments)
            ?: exprEnvironment::class.memberExtensionFunctions.pick(listOf(exprEnvironment) + arguments)
    }
}
