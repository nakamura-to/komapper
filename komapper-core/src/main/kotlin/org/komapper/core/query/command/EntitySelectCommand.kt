package org.komapper.core.query.command

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.SelectContext
import java.sql.ResultSet

internal class EntitySelectCommand<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SelectContext<ENTITY>,
    private val config: DefaultDatabaseConfig,
    override val statement: Statement
) : Command<List<ENTITY>> {

    private val executor: Executor = Executor(config)

    override fun execute(): List<ENTITY> {
        return executor.executeQuery(statement) { rs ->
            // hold only unique entities
            val pool: MutableMap<EntityKey, Any> = mutableMapOf()
            val rows = fetchAllEntities(rs)
            for (row in rows) {
                val entityKeys: MutableMap<EntityMetamodel<*>, EntityKey> = mutableMapOf()
                for ((key, entity) in row) {
                    pool.putIfAbsent(key, entity)
                    entityKeys[key.entityMetamodel] = key
                }
                associate(entityKeys, pool)
            }
            pool.asSequence().filter {
                it.key.entityMetamodel == entityMetamodel
            }.map {
                @Suppress("UNCHECKED_CAST")
                it.value as ENTITY
            }.toList()
        }
    }

    private fun fetchAllEntities(rs: ResultSet): List<Map<EntityKey, Any>> {
        val entityMetamodels = context.getProjectionTargets()
        val rows = mutableListOf<Map<EntityKey, Any>>()
        while (rs.next()) {
            val row = mutableMapOf<EntityKey, Any>()
            var index = 0
            for (entityMetamodel in entityMetamodels) {
                val properties = entityMetamodel.properties()
                val valueMap = mutableMapOf<PropertyMetamodel<*, *>, Any?>()
                for (p in properties) {
                    val value = config.dialect.getValue(rs, ++index, p.klass)
                    valueMap[p] = value
                }
                val entity = checkNotNull(entityMetamodel.instantiate(valueMap))
                val idValues = entityMetamodel.idProperties()
                    .map { it.getWithUncheckedCast(entity) }
                    .map { checkNotNull(it) }
                val key = EntityKey(entityMetamodel, idValues)
                row[key] = entity
            }
            rows.add(row)
        }
        return rows
    }

    private fun associate(
        entityKeys: Map<EntityMetamodel<*>, EntityKey>,
        pool: MutableMap<EntityKey, Any>
    ) {
        for ((association, associator) in context.associatorMap) {
            val key1 = entityKeys[association.first]
            val key2 = entityKeys[association.second]
            if (key1 == null || key2 == null) {
                continue
            }
            val entity1 = pool[key1]!!
            val entity2 = pool[key2]!!
            val newEntity = associator.apply(entity1, entity2)
            pool.replace(key1, newEntity)
        }
    }

    data class EntityKey(
        val entityMetamodel: EntityMetamodel<*>,
        val items: List<Any>
    )
}
