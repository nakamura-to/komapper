package org.komapper.core

import org.komapper.core.expr.CacheExprNodeFactory
import org.komapper.core.expr.DefaultExprEnvironment
import org.komapper.core.expr.DefaultExprEvaluator
import org.komapper.core.expr.ExprEnvironment
import org.komapper.core.expr.ExprEvaluator
import org.komapper.core.expr.ExprNodeFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.logging.Logger
import org.komapper.core.logging.StdoutLogger
import org.komapper.core.template.CacheSqlNodeFactory
import org.komapper.core.template.SqlNodeFactory
import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.TransactionScopeInitiator
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

/**
 * A database configuration.
 *
 * @property dataSource the data source
 * @property dialect the dialect
 * @property name the key which is used to manage sequence values. The name must be unique.
 * @property exprNodeFactory the expression node factory
 * @property exprEnvironment the expression environment
 * @property exprEvaluator the expression evaluator
 * @property sqlNodeFactory the sql node factory
 * @property logger the logger
 * @property isolationLevel the transaction isolation level.
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size. See [PreparedStatement.setFetchSize].
 * @property maxRows the max rows. See [PreparedStatement.setMaxRows].
 * @property queryTimeoutSeconds the query timeout. See [PreparedStatement.setQueryTimeout].
 */
interface DatabaseConfig {
    val dialect: Dialect
    val dataSource: DataSource
    val name: String
    val exprNodeFactory: ExprNodeFactory
    val exprEnvironment: ExprEnvironment
    val exprEvaluator: ExprEvaluator
    val sqlNodeFactory: SqlNodeFactory
    val logger: Logger
    val isolationLevel: TransactionIsolationLevel?
    val batchSize: Int
    val fetchSize: Int?
    val maxRows: Int?
    val queryTimeoutSeconds: Int?
    val transactionScopeInitiator: TransactionScopeInitiator
    val connection: Connection
}

open class DefaultDatabaseConfig(override val dialect: Dialect, override val dataSource: DataSource) :
    DatabaseConfig {

    constructor(
        dialect: Dialect,
        url: String,
        user: String = "",
        password: String = "",
    ) : this(dialect, SimpleDataSource(url, user, password))

    override val name: String = System.identityHashCode(object {}).toString()
    override val exprNodeFactory: ExprNodeFactory by lazy { CacheExprNodeFactory() }
    override val exprEnvironment: ExprEnvironment by lazy {
        DefaultExprEnvironment(dialect::escape)
    }
    override val exprEvaluator: ExprEvaluator by lazy {
        DefaultExprEvaluator(
            exprNodeFactory,
            exprEnvironment
        )
    }
    override val sqlNodeFactory: SqlNodeFactory by lazy { CacheSqlNodeFactory() }
    override val logger: Logger by lazy { StdoutLogger() }
    override val isolationLevel: TransactionIsolationLevel? = null
    override val batchSize: Int = 10
    override val fetchSize: Int? = null
    override val maxRows: Int? = null
    override val queryTimeoutSeconds: Int? = null
    private val transactionManagerDelegate = lazy {
        TransactionManager(dataSource, logger)
    }
    private val transactionManager: TransactionManager by transactionManagerDelegate
    override val transactionScopeInitiator: TransactionScopeInitiator by lazy {
        TransactionScopeInitiator(
            transactionManager,
            isolationLevel
        )
    }
    override val connection: Connection
        get() = if (transactionManagerDelegate.isInitialized())
            transactionManager.getDataSource().connection
        else
            dataSource.connection
}
