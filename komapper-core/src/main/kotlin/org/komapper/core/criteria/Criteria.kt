package org.komapper.core.criteria

import kotlin.reflect.KClass

data class Criteria(
    val type: KClass<*>,
    val joins: List<Join>,
    val where: WhereScope,
    val orderBy: OrderByScope,
    val limit: Int?,
    val offset: Int?,
    val forUpdate: ForUpdateScope?
)
