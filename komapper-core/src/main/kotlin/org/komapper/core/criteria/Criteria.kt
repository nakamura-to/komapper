package org.komapper.core.criteria

data class Criteria(
    val whereScope: WhereScope,
    val orderByScope: OrderByScope,
    val limit: Int?,
    val offset: Int?
)
