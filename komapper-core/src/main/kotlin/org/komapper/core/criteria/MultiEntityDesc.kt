package org.komapper.core.criteria

import org.komapper.core.desc.PropDesc

interface MultiEntityDesc {

    val leafPropDescList: List<PropDesc<*, *>>

    fun new(leafValues: Map<PropDesc<*, *>, Any?>): List<Any>

    fun associate(entity: Any, joinedEntities: List<Any>)
}
