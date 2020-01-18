package org.komapper.core.criteria

import org.komapper.core.dsl.Scope

typealias ForUpdateCriteria = ForUpdateScope.() -> Unit

fun forUpdate(criteria: ForUpdateCriteria): ForUpdateCriteria = criteria

infix operator fun (ForUpdateCriteria).plus(other: ForUpdateCriteria): ForUpdateCriteria {
    val self = this
    return {
        self()
        other()
    }
}

@Scope
class ForUpdateScope(private val update: (ForUpdate) -> Unit) {

    fun nowait(nowait: Boolean = true) {
        update(ForUpdate(nowait))
    }
}
