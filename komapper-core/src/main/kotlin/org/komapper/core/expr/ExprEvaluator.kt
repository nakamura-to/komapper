package org.komapper.core.expr

import org.komapper.core.data.Value
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

interface ExprEvaluator {
    fun eval(expression: String, ctx: Map<String, Value> = emptyMap()): Value
}

open class DefaultExprEvaluator(
    private val exprNodeFactory: ExprNodeFactory,
    private val exprEnvironment: ExprEnvironment,
    private val classResolver: (String) -> Class<*> = { Class.forName(it) }
) : ExprEvaluator {

    // used to distinguish multiple arguments from a single List
    class ArgList : ArrayList<Any?>()

    sealed class ClassRef {
        abstract val clazz: Class<*>

        data class EnumRef(override val clazz: Class<Enum<*>>) : ClassRef()
    }

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
        }.map { it.any }.toCollection(ArgList()).let {
            Value(
                it,
                List::class
            )
        }
        is ExprNode.ClassRef -> visitClassRef(node, ctx)
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

    private fun visitClassRef(node: ExprNode.ClassRef, @Suppress("UNUSED_PARAMETER") ctx: Map<String, Value>): Value {
        val clazz =
            try {
                classResolver(node.name)
            } catch (cause: Exception) {
                throw ExprException("Failed to resolve the class \"${node.name}\" at ${node.location}. The cause is $cause")
            }
        val kClass = clazz.kotlin
        @Suppress("UNCHECKED_CAST")
        return when {
            kClass.objectInstance != null -> Value(kClass.objectInstance!!)
            kClass.companionObjectInstance != null -> Value(kClass.companionObjectInstance!!)
            clazz.isEnum -> Value(ClassRef.EnumRef(clazz as Class<Enum<*>>), kClass)
            else -> error("The unsupported class \"${kClass.qualifiedName}\" is referenced.")
        }
    }

    private fun visitValue(node: ExprNode.Value, ctx: Map<String, Value>): Value {
        return ctx[node.name] ?: exprEnvironment.ctx[node.name] ?: Value(null, Any::class)
    }

    private fun visitProperty(node: ExprNode.Property, ctx: Map<String, Value>): Value {
        val (receiver, receiverType) = visit(node.receiver, ctx)
        if (receiver is ClassRef.EnumRef) {
            val enum = receiver.clazz.enumConstants.first { it.name == node.name }
            return Value(enum)
        }
        val property = findProperty(node.name, receiverType)
            ?: throw ExprException("The property \"${node.name}\" is not found at ${node.location}")
        if (receiver == null && node.safeCall) {
            return Value(null, property.returnType.jvmErasure)
        }
        try {
            // a const property of an object declaration doesn't accept a receiver
            val obj = if (property.isConst && !receiverType.isCompanion)
                property.call()
            else
                property.call(receiver)
            return Value(obj, property.returnType.jvmErasure)
        } catch (cause: Exception) {
            throw ExprException("Failed to call the property \"${node.name}\" at ${node.location}. The cause is $cause")
        }
    }

    private fun findProperty(name: String, receiverType: KClass<*>): KProperty<*>? {
        fun predicate(property: KProperty<*>) =
            name == property.name && property.valueParameters.isEmpty()
        return receiverType.memberProperties.find(::predicate)
            ?: exprEnvironment.topLevelPropertyExtensions.find(::predicate)
    }

    private fun visitFunction(node: ExprNode.Function, ctx: Map<String, Value>): Value {
        fun call(function: KFunction<*>, arguments: List<Any?>): Value {
            try {
                return Value(function.call(*arguments.toTypedArray()), function.returnType.jvmErasure)
            } catch (cause: Exception) {
                throw ExprException("Failed to call the function \"${node.name}\" at ${node.location}. The cause is $cause")
            }
        }

        val (receiver, receiverType) = visit(node.receiver, ctx)
        val (args) = visit(node.args, ctx)
        return if (receiver is ClassRef) {
            findStaticFunction(node.name, receiverType, args)
                ?.let { (function, arguments) -> call(function, arguments) }
                ?: throw ExprException("The static function \"${node.name}\" is not found at ${node.location}")
        } else {
            findFunction(node.name, receiverType, receiver, args)
                ?.let { (function, arguments) ->
                    if (receiver == null && node.safeCall) {
                        Value(null, function.returnType.jvmErasure)
                    } else {
                        call(function, arguments)
                    }
                }
                ?: throw ExprException("The function \"${node.name}\" is not found at ${node.location}")
        }
    }

    private fun findStaticFunction(
        name: String,
        receiverType: KClass<*>,
        args: Any?
    ): Pair<KFunction<*>, List<Any?>>? {
        fun Collection<KFunction<*>>.pick(arguments: List<Any?>): Pair<KFunction<*>, List<Any?>>? {
            return this.filter { function ->
                if (name == function.name && arguments.size == function.parameters.size) {
                    arguments.zip(function.parameters).all { (argument, param) ->
                        argument == null || argument::class.isSubclassOf(param.type.jvmErasure)
                    }
                } else false
            }.map { it to arguments }.firstOrNull()
        }

        val arguments = when (args) {
            Unit -> emptyList()
            is ArgList -> args
            else -> listOf(args)
        }
        return receiverType.staticFunctions.pick(arguments)
    }

    private fun findFunction(
        name: String,
        receiverType: KClass<*>,
        receiver: Any?,
        args: Any?
    ): Pair<KFunction<*>, List<Any?>>? {
        fun Collection<KFunction<*>>.pick(arguments: List<Any?>): Pair<KFunction<*>, List<Any?>>? {
            return this.filter { function ->
                if (name == function.name && arguments.size == function.parameters.size) {
                    arguments.zip(function.parameters).all { (argument, param) ->
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
