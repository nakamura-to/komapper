package org.komapper.core.query.command

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.EntityMetamodel

internal class EntityUpdateCommand<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY,
    config: DefaultDatabaseConfig,
    override val statement: Statement
) : Command<ENTITY> {

    private val executor: Executor = Executor(config)

    override fun execute(): ENTITY {
        return executor.executeUpdate(statement) { _, count ->
            if (entityMetamodel.versionProperty() != null && count != 1) {
                throw OptimisticLockException()
            }
            entityMetamodel.incrementVersion(entity)
        }
    }
}
