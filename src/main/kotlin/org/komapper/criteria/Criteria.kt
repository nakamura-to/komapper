package org.komapper.criteria

data class Criteria<T>(
    val whereScope: WhereScope<T>,
    val orderByScope: OrderByScope<T>,
    val limit: Int?,
    val offset: Int?
)
