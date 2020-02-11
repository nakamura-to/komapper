package org.komapper.core.criteria

import org.komapper.core.dsl.Scope

typealias ForUpdate = ForUpdateScope.() -> Unit

fun forUpdate(block: ForUpdate): ForUpdate = block

infix operator fun (ForUpdate).plus(other: ForUpdate): ForUpdate {
    val self = this
    return {
        self()
        other()
    }
}

data class ForUpdateCriteria(var nowait: Boolean = false)

@Scope
class ForUpdateScope(val _update: (ForUpdateCriteria) -> Unit) {

    fun nowait(nowait: Boolean = true) {
        _update(ForUpdateCriteria(nowait))
    }
}
