package org.komapper.core.desc

interface MultiEntityDesc {

    val leafPropDescList: List<PropDesc>

    fun new(leafValues: Map<PropDesc, Any?>): List<Any>

    fun associate(entity: Any, joinedEntities: List<Any>)
}
