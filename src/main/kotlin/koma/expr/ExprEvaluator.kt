package koma.expr

import kotlin.reflect.KCallable
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

class ExprEvaluator(private val extensions: List<KCallable<Any?>> = emptyList()) {

    init {
        extensions.forEach { it.isAccessible = true }
    }

    // used to distinguish multiple arguments from a single List
    class ArgList : ArrayList<Any?>()

    fun eval(expression: String, ctx: Map<String, Any?> = emptyMap()): Any? {
        val node = ExprParser(expression).parse()
        return visit(node, ctx)
    }

    private fun visit(node: ExprNode, ctx: Map<String, Any?>): Any? = when (node) {
        is Not -> perform(node, ctx) { !it }
        is And -> perform(node, ctx) { x, y -> x && y }
        is Or -> perform(node, ctx) { x, y -> x || y }
        is Eq -> equal(node, ctx) { x, y -> x == y }
        is Ne -> equal(node, ctx) { x, y -> x != y }
        is Ge -> compare(node, ctx) { x, y -> x >= y }
        is Gt -> compare(node, ctx) { x, y -> x > y }
        is Le -> compare(node, ctx) { x, y -> x <= y }
        is Lt -> compare(node, ctx) { x, y -> x < y }
        is Literal -> node.value
        is Comma -> node.nodeList.map { visit(it, ctx) }.toCollection(ArgList())
        is Value -> visitValue(node, ctx)
        is Property -> visitProperty(node, ctx)
        is Function -> visitFunction(node, ctx)
        is Empty -> Unit
    }

    private fun perform(
        node: UnaryOp,
        ctx: Map<String, Any?>,
        f: (Boolean) -> Boolean
    ): Boolean {
        fun checkNull(location: ExprLocation, value: Any?) {
            if (value != null) {
                return
            }
            throw ExprException(
                "Cannot perform the logical operator because the operand is null at $location"
            )
        }

        val operand = visit(node.operand, ctx)
        checkNull(node.operand.location, operand)
        if (operand !is Boolean) {
            throw ExprException(
                "Cannot perform the logical operator because the operands is not Boolean at ${node.location}"
            )
        }
        return f(operand)
    }

    private fun perform(
        node: BinaryOp,
        ctx: Map<String, Any?>,
        f: (Boolean, Boolean) -> Boolean
    ): Boolean {
        fun checkNull(location: ExprLocation, value: Any?, which: String) {
            if (value != null) {
                return
            }
            throw ExprException(
                "Cannot perform the logical operator because the $which operand is null at $location"
            )
        }

        val left = visit(node.left, ctx)
        val right = visit(node.right, ctx)
        checkNull(node.left.location, left, "left")
        checkNull(node.right.location, right, "right")
        if (left !is Boolean || right !is Boolean) {
            throw ExprException(
                "Cannot perform the logical operator because either operands is not Boolean at ${node.location}"
            )
        }
        return f(left, right)
    }

    private fun equal(
        node: BinaryOp,
        ctx: Map<String, Any?>,
        f: (Any?, Any?) -> Boolean
    ): Boolean {
        val left = visit(node.left, ctx)
        val right = visit(node.right, ctx)
        return f(left, right)
    }

    @Suppress("UNCHECKED_CAST")
    private fun compare(
        node: BinaryOp,
        ctx: Map<String, Any?>,
        f: (Comparable<Any>, Comparable<Any>) -> Boolean
    ): Boolean {
        fun checkNull(location: ExprLocation, value: Any?, which: String) {
            if (value != null) {
                return
            }
            throw ExprException(
                "Cannot compare because the $which operand is null at $location"
            )
        }

        val left = visit(node.left, ctx)
        val right = visit(node.right, ctx)
        checkNull(node.left.location, left, "left")
        checkNull(node.right.location, right, "right")
        try {
            left as Comparable<Any>
            right as Comparable<Any>
            return f(left, right)
        } catch (e: ClassCastException) {
            throw ExprException(
                "Cannot compare because the operands are not comparable to each other at ${node.location}"
            )
        }
    }

    private fun visitValue(node: Value, ctx: Map<String, Any?>): Any? {
        return ctx[node.name]
    }

    private fun visitProperty(node: Property, ctx: Map<String, Any?>): Any? {
        val receiver = visit(node.receiver, ctx)
        val property = findProperty(node.name, receiver)
            ?: throw ExprException("The receiver of the property \"${node.name}\" is null or the property is not found at ${node.location}")
        try {
            return property.call(receiver)
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

    private fun visitFunction(node: Function, ctx: Map<String, Any?>): Any? {
        val receiver = visit(node.receiver, ctx)
        // arguments for KCallable
        val args = when (val value = visit(node.args, ctx)) {
            Unit -> listOf(receiver)
            is ArgList -> listOf(receiver) + value
            else -> listOf(receiver, value)
        }
        val function = findFunction(node.name, receiver, args)
            ?: throw ExprException("The receiver of the function \"${node.name}\" is null or the function is not found at ${node.location}")
        try {
            return function.call(*args.toTypedArray())
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