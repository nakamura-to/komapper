package org.komapper.core.desc

import java.util.concurrent.ConcurrentHashMap

sealed class PropKind {
    sealed class Id : PropKind() {
        object Assign : Id()
        data class Sequence(
            private val name: String,
            private val incrementBy: Int,
            private val cast: (Long) -> Any
        ) :
            Id() {
            private val cache = ConcurrentHashMap<String, IdGenerator>()

            fun next(key: String, callNextValue: (String) -> Long): Any {
                val generator = cache.computeIfAbsent(key) {
                    IdGenerator(incrementBy) { callNextValue(name) }
                }
                return generator.next().let(cast)
            }
        }
    }

    data class CreatedAt(val now: () -> Any) : PropKind()
    data class UpdatedAt(val now: () -> Any) : PropKind()
    data class Version(val inc: (Any) -> Any) : PropKind()
    data class Embedded(val embeddedDesc: EmbeddedDesc) : PropKind()
    object Basic : PropKind()
}
