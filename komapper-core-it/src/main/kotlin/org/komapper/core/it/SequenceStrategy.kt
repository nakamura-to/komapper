package org.komapper.core.it

import org.komapper.core.metadata.EntityMetadata
import org.komapper.core.metadata.SequenceGenerator

data class SequenceStrategy(
    val id: Int = 0,
    val value: String
)

object SequenceStrategyMetadata : EntityMetadata<SequenceStrategy>({
    id(SequenceStrategy::id, SequenceGenerator("SEQUENCE_STRATEGY_ID", 100))
    table {
        name("sequence_strategy", true)
        column(SequenceStrategy::id, "id", true)
        column(SequenceStrategy::value, "value", true)
    }
})
