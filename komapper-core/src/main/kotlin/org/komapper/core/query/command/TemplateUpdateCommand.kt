package org.komapper.core.query.command

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor

internal class TemplateUpdateCommand(
    private val config: DefaultDatabaseConfig,
    override val statement: Statement
) : Command<Int> {

    private val executor: Executor = Executor(config)

    override fun execute(): Int {
        return executor.executeUpdate(statement) { _, count -> count }
    }
}
