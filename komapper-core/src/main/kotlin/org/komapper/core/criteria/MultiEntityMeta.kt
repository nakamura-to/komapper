package org.komapper.core.criteria

import org.komapper.core.meta.PropMeta

interface MultiEntityMeta {

    val leafPropMetaList: List<PropMeta<*, *>>

    fun new(leafValues: Map<PropMeta<*, *>, Any?>): List<Any>

    fun associate(entity: Any, joinedEntities: List<Any>)
}
