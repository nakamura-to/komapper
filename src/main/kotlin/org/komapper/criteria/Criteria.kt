package org.komapper.criteria

data class Criteria(
    val whereScope: WhereScope,
    val orderByScope: OrderByScope,
    val limit: Int?,
    val offset: Int?
)
