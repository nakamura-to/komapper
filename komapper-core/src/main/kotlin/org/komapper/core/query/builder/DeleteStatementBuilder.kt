package org.komapper.core.query.builder

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.DeleteContext

internal class DeleteStatementBuilder<ENTITY>(
    val config: DefaultDatabaseConfig,
    val context: DeleteContext<ENTITY>,
    val entity: ENTITY
) {
    private val aliasManager = AliasManager(context)
    private val buf = StatementBuffer(config.dialect::formatValue)

    fun build(): Statement {
        buf.append("delete from ")
        buf.append(tableName(context.entityMetamodel))
        val identityProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        if (identityProperties.isNotEmpty() || versionProperty != null) {
            buf.append(" where ")
            if (identityProperties.isNotEmpty()) {
                for (p in identityProperties) {
                    buf.append(columnName(p))
                    buf.append(" = ")
                    val value = Value(p.get(entity), p.klass)
                    buf.bind(value)
                    buf.append(" and ")
                }
            }
            if (versionProperty != null) {
                buf.append(columnName(versionProperty))
                buf.append(" = ")
                val value = Value(versionProperty.get(entity), versionProperty.klass)
                buf.bind(value)
            }
        }
        return buf.toStatement()
    }

    private fun tableName(entityMetamodel: EntityMetamodel<*>): String {
        val alias = aliasManager.getAlias(entityMetamodel) ?: error("no alias")
        return entityMetamodel.tableName() + " " + alias
    }

    private fun columnName(propertyMetamodel: PropertyMetamodel<*, *>): String {
        val alias = aliasManager.getAlias(propertyMetamodel) ?: error("no alias")
        return alias + "." + propertyMetamodel.columnName
    }
}
