package org.komapper.core.criteria

import kotlin.reflect.KClass

data class Join(
    val kind: JoinKind,
    val type: KClass<*>,
    val onScope: OnScope,
    val block: (Any, Any) -> Unit
)

enum class JoinKind {
    INNER,
    LEFT
}
