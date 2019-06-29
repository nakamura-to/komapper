package org.komapper.core.it

import org.komapper.core.Column
import org.komapper.core.Id
import org.komapper.core.SequenceGenerator
import org.komapper.core.Table

@Table(name = "sequence_strategy", quote = true)
data class SequenceStrategy(
    @Id
    @SequenceGenerator(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100)
    @Column(name = "id", quote = true)
    val id: Int = 0,
    @Column(name = "value", quote = true)
    val value: String
)
