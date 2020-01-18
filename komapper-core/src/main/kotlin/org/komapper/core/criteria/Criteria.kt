package org.komapper.core.criteria

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.komapper.core.dsl.EmptyScope

data class Criteria<T : Any>(
    val kClass: KClass<T>,
    val joins: MutableList<Join<Any, Any>> = mutableListOf(),
    val where: MutableList<Criterion> = mutableListOf(),
    val orderBy: MutableList<Pair<KProperty1<*, *>, String>> = mutableListOf(),
    var limit: Int? = null,
    var offset: Int? = null,
    var forUpdate: ForUpdate? = null
)

sealed class Criterion {
    data class Eq(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ne(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Gt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Ge(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Lt(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class Le(val prop: KProperty1<*, *>, val value: Any?) : Criterion()
    data class In(val prop: KProperty1<*, *>, val values: List<*>) : Criterion()
    data class NotIn(val prop: KProperty1<*, *>, val values: List<*>) : Criterion()
    data class In2(val props: Pair<KProperty1<*, *>, KProperty1<*, *>>, val values: List<Pair<*, *>>) : Criterion()
    data class NotIn2(val props: Pair<KProperty1<*, *>, KProperty1<*, *>>, val values: List<Pair<*, *>>) :
        Criterion()

    data class In3(
        val props: Triple<KProperty1<*, *>, KProperty1<*, *>, KProperty1<*, *>>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class NotIn3(
        val props: Triple<KProperty1<*, *>, KProperty1<*, *>, KProperty1<*, *>>,
        val values: List<Triple<*, *, *>>
    ) : Criterion()

    data class Like(val prop: KProperty1<*, *>, val value: String?) : Criterion()
    data class NotLike(val prop: KProperty1<*, *>, val value: String?) : Criterion()
    data class Between(val prop: KProperty1<*, *>, val range: Pair<*, *>) : Criterion()
    data class Not(val criteria: List<Criterion>) : Criterion()
    data class And(val criteria: List<Criterion>) : Criterion()
    data class Or(val criteria: List<Criterion>) : Criterion()
}

data class Join<T : Any, S : Any>(
    val kind: JoinKind,
    val type: KClass<S>,
    val on: MutableList<Criterion> = mutableListOf(),
    var association: EmptyScope.(T, S) -> Unit = { _, _ -> }
)

enum class JoinKind {
    INNER,
    LEFT
}

data class ForUpdate(var nowait: Boolean = false)
