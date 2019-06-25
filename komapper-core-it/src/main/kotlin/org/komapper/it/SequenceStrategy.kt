package org.komapper.it

import org.komapper.Column
import org.komapper.Id
import org.komapper.SequenceGenerator
import org.komapper.Table

@Table(name = "sequence_strategy", quote = true)
data class SequenceStrategy(
    @Id
    @SequenceGenerator(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100)
    @Column(name = "id", quote = true)
    val id: Int = 0,
    @Column(name = "value", quote = true)
    val value: String
)
