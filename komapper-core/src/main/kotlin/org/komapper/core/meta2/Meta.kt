package org.komapper.core.meta2

import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class TableMeta(val typeName: String?, val quote: Boolean)
data class ColumnMeta(val propName: String, val columnName: String?, val quote: Boolean)
data class CreatedAtMeta(val propName: String)
data class UpdatedAtMeta(val propName: String)
data class IdMeta(val propName: String)
data class VersionMeta(val propName: String)

@DslMarker
annotation class MetaMarker

@MetaMarker
class MetaScope<T : Any>() {
    internal val tableScope = TableScope<T>()
    internal val entityScope = EntityScope<T>()
    fun table(block: TableScope<T>.() -> Unit) = tableScope.block()
    fun entity(block: EntityScope<T>.() -> Unit) = entityScope.block()
}

@MetaMarker
class TableScope<T : Any>() {
    internal var tableMeta: TableMeta = TableMeta(null, false)
    internal val columnMetaList = mutableListOf<ColumnMeta>()
    fun name(kClass: KClass<T>, quote: Boolean = false) {
        tableMeta = TableMeta(kClass.simpleName, quote)
    }
    fun column(property: KProperty1<T, *>, name: String? = null, quote: Boolean = false) {
        val columnMeta = ColumnMeta(property.name, name, quote)
        columnMetaList.add(columnMeta)
    }
}

@MetaMarker
class EntityScope<T : Any>() {
    internal var idScope = IdScope<T>()
    internal var versionMeta: VersionMeta? = null
    internal var createdAtMeta: CreatedAtMeta? = null
    internal var updatedAtMeta: UpdatedAtMeta? = null

    fun id(block: IdScope<T>.() -> Unit) = idScope.block()

    fun version(property: KProperty1<T, Int>, initialValue: Int = 0) {
        versionMeta = VersionMeta(property.name)
    }

    fun version(property: KProperty1<T, Int?>, initialValue: Int? = 0) {
        versionMeta = VersionMeta(property.name)
    }

    fun version(property: KProperty1<T, Long>, initialValue: Long = 0L) {
        versionMeta = VersionMeta(property.name)
    }

    fun version(property: KProperty1<T, Long?>, initialValue: Long? = 0) {
        versionMeta = VersionMeta(property.name)
    }

    fun createdAt(
        property: KProperty1<T, LocalDateTime?>,
        initialValue: () -> LocalDateTime = { LocalDateTime.now() }
    ) {
        createdAtMeta = CreatedAtMeta(property.name)
    }

    fun updatedAt(
        property: KProperty1<T, LocalDateTime?>,
        initialValue: () -> LocalDateTime = { LocalDateTime.now() }
    ) {
        updatedAtMeta = UpdatedAtMeta(property.name)
    }

    fun embedded(property: KProperty1<T, *>) {
    }

    fun listener(property: KProperty1<T, *>) {
    }
}

@MetaMarker
class IdScope<T : Any>() {
    val idMetaList = mutableListOf<IdMeta>()

    fun assign(property: KProperty1<T, *>) {
        var idMeta = IdMeta(property.name)
        idMetaList.add(idMeta)
    }

    fun generate(property: KProperty1<T, *>) {
        var idMeta = IdMeta(property.name)
        idMetaList.add(idMeta)
    }
}

data class Metadata(
    val table: TableMeta?,
    val columnList: List<ColumnMeta>,
    val idList: List<IdMeta>,
    val version: VersionMeta?,
    val createdAt: CreatedAtMeta?,
    val updatedAt: UpdatedAtMeta?
)

abstract class Meta<T : Any>(val block: MetaScope<T>.() -> Unit, val prefix: String = "", val suffix: String = "Meta") {
    val metadata: Metadata

    init {
        val metaScope = MetaScope<T>()
        metaScope.block()
        metadata =
            Metadata(
                metaScope.tableScope.tableMeta,
                metaScope.tableScope.columnMetaList,
                metaScope.entityScope.idScope.idMetaList,
                metaScope.entityScope.versionMeta,
                metaScope.entityScope.createdAtMeta,
                metaScope.entityScope.updatedAtMeta
            )
    }
}
