package org.komapper.core.metadata

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Metadata<T : Any> {
    val table: TableMeta
    val columnList: List<ColumnMeta>
    val idList: List<IdMeta>
    val version: VersionMeta?
    val createdAt: CreatedAtMeta?
    val updatedAt: UpdatedAtMeta?
    val embeddedList: List<EmbeddedMeta>
}

abstract class EntityMetadata<T : Any>(val block: EntityScope<T>.() -> Unit = {}) :
    Metadata<T> {
    private val entityScope = EntityScope<T>()

    override val table: TableMeta
        get() = entityScope.tableScope.table

    override val columnList: List<ColumnMeta>
        get() = entityScope.tableScope.columnList

    override val idList: List<IdMeta>
        get() = entityScope.idList

    override val version: VersionMeta?
        get() = entityScope.version

    override val createdAt: CreatedAtMeta?
        get() = entityScope.createdAt

    override val updatedAt: UpdatedAtMeta?
        get() = entityScope.updatedAt

    override val embeddedList: List<EmbeddedMeta>
        get() = entityScope.embeddedList

    init {
        entityScope.block()
    }
}

data class TableMeta(val name: String?, val quote: Boolean)
data class ColumnMeta(val propName: String, val name: String?, val quote: Boolean)
data class CreatedAtMeta(val propName: String)
data class UpdatedAtMeta(val propName: String)
sealed class IdMeta {
    abstract val propName: String

    data class PlainId(override val propName: String) : IdMeta()
    data class SequenceId(
        override val propName: String,
        val generator: SequenceGenerator
    ) : IdMeta()
}

data class VersionMeta(val propName: String)
data class EmbeddedMeta(val propName: String)
data class SequenceGenerator(
    val name: String,
    val incrementBy: Int = 1,
    val quote: Boolean = false
)

@DslMarker
annotation class MetaMarker

@MetaMarker
class EntityScope<T : Any> {
    internal val tableScope = TableScope<T>()
    internal val idList = mutableListOf<IdMeta>()
    internal var version: VersionMeta? = null
    internal var createdAt: CreatedAtMeta? = null
    internal var updatedAt: UpdatedAtMeta? = null
    internal val embeddedList = mutableListOf<EmbeddedMeta>()

    fun table(block: TableScope<T>.() -> Unit) = tableScope.block()

    fun id(property: KProperty1<T, *>) {
        val idMeta = IdMeta.PlainId(property.name)
        idList.add(idMeta)
    }

    fun id(property: KProperty1<T, *>, generator: SequenceGenerator) {
        val idMeta = IdMeta.SequenceId(property.name, generator)
        idList.add(idMeta)
    }

    fun version(property: KProperty1<T, *>) {
        version = VersionMeta(property.name)
    }

    fun createdAt(
        property: KProperty1<T, *>
    ) {
        createdAt = CreatedAtMeta(property.name)
    }

    fun updatedAt(
        property: KProperty1<T, *>
    ) {
        updatedAt = UpdatedAtMeta(property.name)
    }

    fun embedded(property: KProperty1<T, *>) {
        var embedded = EmbeddedMeta(property.name)
        embeddedList.add(embedded)
    }

    fun listener(property: KProperty1<T, *>) {
    }
}

@MetaMarker
class TableScope<T : Any>() {
    internal var table: TableMeta = TableMeta(null, false)
    internal val columnList = mutableListOf<ColumnMeta>()
    fun name(name: String, quote: Boolean = false) {
        table = TableMeta(name, quote)
    }

    fun name(kClass: KClass<T>, quote: Boolean = false) {
        table = TableMeta(kClass.simpleName, quote)
    }

    fun column(property: KProperty1<T, *>, name: String? = null, quote: Boolean = false) {
        val column = ColumnMeta(property.name, name, quote)
        columnList.add(column)
    }
}
