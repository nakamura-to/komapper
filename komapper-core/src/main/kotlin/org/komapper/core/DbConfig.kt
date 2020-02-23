package org.komapper.core

import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource
import org.komapper.core.builder.DefaultEntityStmtBuilder
import org.komapper.core.builder.EntityStmtBuilder
import org.komapper.core.entity.CamelToSnake
import org.komapper.core.entity.DataDescFactory
import org.komapper.core.entity.DefaultDataDescFactory
import org.komapper.core.entity.DefaultEntityDescFactory
import org.komapper.core.entity.DefaultGlobalEntityListener
import org.komapper.core.entity.DefaultPropDescFactory
import org.komapper.core.entity.EntityDescFactory
import org.komapper.core.entity.EntityMetaResolver
import org.komapper.core.entity.GlobalEntityListener
import org.komapper.core.entity.NamingStrategy
import org.komapper.core.entity.PropDescFactory
import org.komapper.core.expr.CacheExprNodeFactory
import org.komapper.core.expr.DefaultExprEnvironment
import org.komapper.core.expr.DefaultExprEvaluator
import org.komapper.core.expr.ExprEnvironment
import org.komapper.core.expr.ExprEvaluator
import org.komapper.core.expr.ExprNodeFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.logging.Logger
import org.komapper.core.logging.StdoutLogger
import org.komapper.core.sql.AnyDescFactory
import org.komapper.core.sql.CacheAnyDescFactory
import org.komapper.core.sql.CacheSqlNodeFactory
import org.komapper.core.sql.DefaultStmtBuilder
import org.komapper.core.sql.DefaultTemplateRewriter
import org.komapper.core.sql.SqlNodeFactory
import org.komapper.core.sql.StmtBuilder
import org.komapper.core.sql.TemplateRewriter
import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.TransactionScopeInitiator

/**
 * A database configuration.
 *
 * @property dataSource the data source
 * @property dialect the dialect
 * @property name the key which is used to manage sequence values. The name must be unique.
 * @property namingStrategy the naming strategy for entity classes and properties.
 * @property entityMetaResolver the entity metadata resolver
 * @property anyDescFactory the object description factory
 * @property dataDescFactory the data class description factory
 * @property propDescFactory the property description factory
 * @property entityDescFactory the entity description factory
 * @property listener the global entity listener
 * @property entityStmtBuilder the sql statement builder for entities
 * @property exprNodeFactory the expression node factory
 * @property exprEnvironment the expression environment
 * @property exprEvaluator the expression evaluator
 * @property sqlNodeFactory the sql node factory
 * @property templateRewriter the template rewriter
 * @property stmtBuilder the sql statement builder
 * @property logger the logger
 * @property isolationLevel the transaction isolation level.
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size. See [PreparedStatement.setFetchSize].
 * @property maxRows the max rows. See [PreparedStatement.setMaxRows].
 * @property queryTimeout the query timeout. See [PreparedStatement.setQueryTimeout].
 */
abstract class DbConfig() {
    abstract val dataSource: DataSource
    abstract val dialect: Dialect
    abstract val entityMetaResolver: EntityMetaResolver
    open val name: String = System.identityHashCode(object {}).toString()
    open val namingStrategy: NamingStrategy by lazy { CamelToSnake() }
    open val anyDescFactory: AnyDescFactory by lazy { CacheAnyDescFactory() }
    open val propDescFactory: PropDescFactory by lazy {
        DefaultPropDescFactory(
            dialect::quote,
            namingStrategy
        )
    }
    open val dataDescFactory: DataDescFactory by lazy {
        DefaultDataDescFactory(
            entityMetaResolver,
            propDescFactory
        )
    }
    open val entityDescFactory: EntityDescFactory by lazy {
        DefaultEntityDescFactory(
            dataDescFactory,
            dialect::quote,
            namingStrategy
        )
    }
    open val listener: GlobalEntityListener by lazy { DefaultGlobalEntityListener() }
    open val entityStmtBuilder: EntityStmtBuilder by lazy {
        DefaultEntityStmtBuilder(
            dialect,
            entityDescFactory
        )
    }
    open val exprNodeFactory: ExprNodeFactory by lazy { CacheExprNodeFactory() }
    open val exprEnvironment: ExprEnvironment by lazy {
        DefaultExprEnvironment(dialect::escape)
    }
    open val exprEvaluator: ExprEvaluator by lazy {
        DefaultExprEvaluator(
            exprNodeFactory,
            exprEnvironment
        )
    }
    open val sqlNodeFactory: SqlNodeFactory by lazy { CacheSqlNodeFactory() }
    open val templateRewriter: TemplateRewriter by lazy { DefaultTemplateRewriter() }
    open val stmtBuilder: StmtBuilder by lazy {
        DefaultStmtBuilder(
            dialect::formatValue,
            anyDescFactory,
            sqlNodeFactory,
            exprEvaluator
        )
    }
    open val logger: Logger by lazy { StdoutLogger() }
    open val isolationLevel: TransactionIsolationLevel? = null
    open val batchSize: Int = 10
    open val fetchSize: Int? = null
    open val maxRows: Int? = null
    open val queryTimeout: Int? = null
    private val transactionManagerDelegate = lazy {
        TransactionManager(dataSource, logger)
    }
    val transactionManager: TransactionManager by transactionManagerDelegate
    val transactionScopeInitiator: TransactionScopeInitiator by lazy {
        TransactionScopeInitiator(
            transactionManager,
            isolationLevel
        )
    }
    val connection: Connection
        get() = if (transactionManagerDelegate.isInitialized())
            transactionManager.getDataSource().connection
        else
            dataSource.connection
}
