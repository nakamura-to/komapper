package org.komapper.core.criteria

import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.sql.Sql
import org.komapper.core.sql.SqlBuffer
import org.komapper.core.value.Value

class InsertProcessor(
    private val dialect: Dialect,
    private val entityDescFactory: EntityDescFactory,
    private val criteria: InsertCriteria<*>,
    private val buf: SqlBuffer = SqlBuffer(dialect::formatValue)
) {

    private val entityDesc: EntityDesc<*> = entityDescFactory.get(criteria.kClass)

    fun buildInsert(): Sql {
        buf.append("insert into ${entityDesc.tableName} (")
        with(criteria) {
            if (values.isNotEmpty()) {
                values.forEach { (prop, obj) ->
                    val propDesc = entityDesc.propMap[prop.kProperty1] ?: error("The propDesc is not found.")
                    buf.append("${propDesc.columnName}, ")
                }
                buf.cutBack(2)
            }
            buf.append(") values (")
            if (values.isNotEmpty()) {
                values.forEach { (prop, obj) ->
                    val value = Value(obj, prop.kProperty1.returnType)
                    buf.bind(value).append(", ")
                }
                buf.cutBack(2)
            }
        }
        buf.append(")")
        return buf.toSql()
    }
}
