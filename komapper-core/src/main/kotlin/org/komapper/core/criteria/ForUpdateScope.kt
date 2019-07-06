package org.komapper.core.criteria

@CriteriaMarker
class ForUpdateScope {

    internal var nowait: Boolean = false

    fun nowait() {
        nowait = true
    }
}
