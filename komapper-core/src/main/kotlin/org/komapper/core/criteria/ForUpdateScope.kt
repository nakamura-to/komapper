package org.komapper.core.criteria

@CriteriaMarker
class ForUpdateScope(private val criteria: MutableCriteria<*>) {

    fun nowait() {
        criteria.forUpdate = ForUpdate(true)
    }
}
