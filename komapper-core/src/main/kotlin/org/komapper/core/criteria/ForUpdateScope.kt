package org.komapper.core.criteria

import org.komapper.core.dsl.Scope

@Scope
class ForUpdateScope(private val criteria: MutableCriteria<*>) {

    fun nowait() {
        criteria.forUpdate = ForUpdate(true)
    }
}
