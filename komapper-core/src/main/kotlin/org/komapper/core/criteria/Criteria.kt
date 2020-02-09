package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope

data class Criteria<T : Any>(
    val kClass: KClass<T>,
    val alias: Alias = Alias(),
    var distinct: Boolean = false,
    val joins: MutableList<JoinCriteria<Any, Any>> = mutableListOf(),
    val where: MutableList<Criterion> = mutableListOf(),
    val orderBy: MutableList<OrderByItem> = mutableListOf(),
    var limit: Int? = null,
    var offset: Int? = null,
    var forUpdate: ForUpdateCriteria? = null
)

data class DeleteCriteria<T : Any>(
    val kClass: KClass<T>,
    val alias: Alias = Alias(),
    val where: MutableList<Criterion> = mutableListOf()
)

sealed class Criterion {
    data class Eq(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Ne(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Gt(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Ge(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Lt(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class Le(val prop: AliasProperty<*, *>, val value: Any?) : Criterion()
    data class In(val prop: AliasProperty<*, *>, val values: List<*>) : Criterion()
    data class NotIn(val prop: AliasProperty<*, *>, val values: List<*>) : Criterion()
    data class In2(val prop1: AliasProperty<*, *>, val prop2: AliasProperty<*, *>, val values: List<Pair<*, *>>) :
        Criterion()

    data class NotIn2(val prop1: AliasProperty<*, *>, val prop2: AliasProperty<*, *>, val values: List<Pair<*, *>>) :
        Criterion()

    data class In3(
        val prop1: AliasProperty<*, *>,
        val prop2: AliasProperty<*, *>,
        val prop3: AliasProperty<*, *>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class NotIn3(
        val prop1: AliasProperty<*, *>,
        val prop2: AliasProperty<*, *>,
        val prop3: AliasProperty<*, *>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class Like(val prop: AliasProperty<*, *>, val value: String?) : Criterion()
    data class NotLike(val prop: AliasProperty<*, *>, val value: String?) : Criterion()
    data class Between(val prop: AliasProperty<*, *>, val range: Pair<*, *>) : Criterion()
    data class Exists(val criteria: Criteria<*>) : Criterion()
    data class NotExists(val criteria: Criteria<*>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
}

data class JoinCriteria<T : Any, S : Any>(
    val kind: JoinKind,
    val type: KClass<S>,
    val alias: Alias,
    val on: MutableList<Criterion> = mutableListOf(),
    var association: EmptyScope.(T, S) -> Unit = { _, _ -> }
)

enum class JoinKind {
    INNER,
    LEFT
}

data class OrderByItem(val prop: AliasProperty<*, *>, val sort: String)

data class ForUpdateCriteria(var nowait: Boolean = false)

class Alias(private val parent: Alias? = null) {
    private var counter = 0
    private val index = if (parent != null) parent.counter++ else counter++
    val name = "t${index}_"
    operator fun <T, R> get(prop: KProperty1<T, R>): AliasProperty<T, R> = AliasProperty(this, prop)
    override fun toString(): String = name
    fun next(): Alias = Alias(this)
}

data class AliasProperty<T, R>(
    val alias: Alias,
    val kProperty1: KProperty1<T, R>
)
