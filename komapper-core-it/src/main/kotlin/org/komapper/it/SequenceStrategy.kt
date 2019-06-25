package org.komapper.it

import org.komapper.Id
import org.komapper.SequenceGenerator

data class SequenceStrategy(
    @Id
    @SequenceGenerator(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100)
    val id: Int = 0,
    val value: String
)
