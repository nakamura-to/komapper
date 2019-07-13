package org.komapper.core

import org.komapper.core.expr.*
import org.komapper.core.jdbc.Dialect
import org.komapper.core.logging.Logger
import org.komapper.core.logging.StdoutLogger
import org.komapper.core.meta.*
import org.komapper.core.sql.*
import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.TransactionScope
import java.sql.Connection
import javax.sql.DataSource

data class DbConfig(
    val name: String = System.identityHashCode(object {}).toString(),
    val dataSource: DataSource,
    val dialect: Dialect,
    val namingStrategy: NamingStrategy = CamelToSnake(),
    val objectMetaFactory: ObjectMetaFactory = DefaultObjectMetaFactory(),
    val embeddedMetaFactory: EmbeddedMetaFactory = DefaultEmbeddedMetaFactory(),
    val propMetaFactory: PropMetaFactory = DefaultPropMetaFactory(
        dialect::quote,
        namingStrategy,
        embeddedMetaFactory
    ),
    val entityMetaFactory: EntityMetaFactory = DefaultEntityMetaFactory(
        dialect::quote,
        namingStrategy,
        propMetaFactory
    ),
    val listener: EntityListener = DefaultEntityListener(),
    val entitySqlBuilder: EntitySqlBuilder = DefaultEntitySqlBuilder(
        dialect::formatValue
    ),
    val exprNodeFactory: ExprNodeFactory = CacheExprNodeFactory(),
    val exprEnvironment: ExprEnvironment = DefaultExprEnvironment(dialect::escape),
    val exprEvaluator: ExprEvaluator = DefaultExprEvaluator(
        exprNodeFactory,
        exprEnvironment
    ),
    val sqlNodeFactory: SqlNodeFactory = CacheSqlNodeFactory(),
    val sqlRewriter: SqlRewriter = DefaultSqlRewriter(sqlNodeFactory),
    val sqlBuilder: SqlBuilder = DefaultSqlBuilder(
        dialect::formatValue,
        sqlNodeFactory,
        exprEvaluator
    ),
    val logger: Logger = StdoutLogger(),
    val useTransaction: Boolean = false,
    val isolationLevel: TransactionIsolationLevel? = null,
    val batchSize: Int = 10,
    val fetchSize: Int? = null,
    val maxRows: Int? = null,
    val queryTimeout: Int? = null
) {

    val transactionManager: TransactionManager by lazy {
        check(useTransaction) { "To use transaction, specify \"useTransaction = true\" at DbConfig." }
        TransactionManager(dataSource, logger)
    }

    val transactionScope: TransactionScope
        get() = if (useTransaction) {
            TransactionScope(transactionManager, isolationLevel)
        } else {
            error("To use transaction, specify \"useTransaction = true\" at DbConfig.")
        }

    val connection: Connection
        get() = if (useTransaction)
            transactionManager.getDataSource().connection
        else
            dataSource.connection
}

