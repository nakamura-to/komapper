package org.komapper.core.meta

import java.util.concurrent.ConcurrentHashMap

sealed class PropKind<T> {
    sealed class Id<T> : PropKind<T>() {
        object Assign : Id<Any?>()
        data class Sequence<T>(
            private val name: String,
            private val incrementBy: Int,
            private val cast: (Long) -> T
        ) :
            Id<T>() {
            private val cache = ConcurrentHashMap<String, IdGenerator>()

            fun next(key: String, callNextValue: (String) -> Long): T {
                val generator = cache.computeIfAbsent(key) {
                    IdGenerator(incrementBy) { callNextValue(name) }
                }
                return generator.next().let(cast)
            }
        }
    }

    data class CreatedAt<T>(val now: () -> T) : PropKind<T>()
    data class UpdatedAt<T>(val now: () -> T) : PropKind<T>()
    data class Version<T>(val inc: (T) -> T) : PropKind<T>()
    data class Embedded<T>(val embeddedMeta: EmbeddedMeta<T>) : PropKind<T>()
    object Basic : PropKind<Any?>()
}
