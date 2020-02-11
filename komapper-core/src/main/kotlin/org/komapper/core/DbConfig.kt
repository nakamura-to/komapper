package org.komapper.core

import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource
import org.komapper.core.builder.DefaultEntitySqlBuilder
import org.komapper.core.builder.EntitySqlBuilder
import org.komapper.core.desc.CamelToSnake
import org.komapper.core.desc.DataDescFactory
import org.komapper.core.desc.DefaultDataDescFactory
import org.komapper.core.desc.DefaultEntityDescFactory
import org.komapper.core.desc.DefaultGlobalEntityListener
import org.komapper.core.desc.DefaultObjectDescFactory
import org.komapper.core.desc.DefaultPropDescFactory
import org.komapper.core.desc.EntityDescFactory
import org.komapper.core.desc.GlobalEntityListener
import org.komapper.core.desc.NamingStrategy
import org.komapper.core.desc.ObjectDescFactory
import org.komapper.core.desc.PropDescFactory
import org.komapper.core.expr.CacheExprNodeFactory
import org.komapper.core.expr.DefaultExprEnvironment
import org.komapper.core.expr.DefaultExprEvaluator
import org.komapper.core.expr.ExprEnvironment
import org.komapper.core.expr.ExprEvaluator
import org.komapper.core.expr.ExprNodeFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.logging.Logger
import org.komapper.core.logging.StdoutLogger
import org.komapper.core.metadata.MetadataResolver
import org.komapper.core.sql.CacheSqlNodeFactory
import org.komapper.core.sql.DefaultSqlBuilder
import org.komapper.core.sql.DefaultSqlRewriter
import org.komapper.core.sql.SqlBuilder
import org.komapper.core.sql.SqlNodeFactory
import org.komapper.core.sql.SqlRewriter
import org.komapper.core.tx.TransactionIsolationLevel
import org.komapper.core.tx.TransactionManager
import org.komapper.core.tx.TransactionScope

/**
 * A database configuration.
 *
 * @property dataSource the data source
 * @property dialect the dialect
 * @property name the key which is used to manage sequence values. The name must be unique.
 * @property namingStrategy the naming strategy for entity classes and properties.
 * @property metadataResolver the metadata resolver
 * @property objectDescFactory the object description factory
 * @property dataDescFactory the data class description factory
 * @property propDescFactory the property description factory
 * @property entityDescFactory the entity description factory
 * @property listener the entity listener
 * @property entitySqlBuilder the sql builder for entities
 * @property exprNodeFactory the expression node factory
 * @property exprEnvironment the expression environment
 * @property exprEvaluator the expression evaluator
 * @property sqlNodeFactory the sql node factory
 * @property sqlRewriter the sql rewriter
 * @property sqlBuilder the sql builder
 * @property logger the logger
 * @property isolationLevel the isolation level.
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size. See [PreparedStatement.setFetchSize].
 * @property maxRows the max rows. See [PreparedStatement.setMaxRows].
 * @property queryTimeout the query timeout. See [PreparedStatement.setQueryTimeout].
 */
abstract class DbConfig() {
    abstract val dataSource: DataSource
    abstract val dialect: Dialect
    abstract val metadataResolver: MetadataResolver
    open val name: String = System.identityHashCode(object {}).toString()
    open val namingStrategy: NamingStrategy by lazy { CamelToSnake() }
    open val objectDescFactory: ObjectDescFactory by lazy { DefaultObjectDescFactory() }
    open val propDescFactory: PropDescFactory by lazy {
        DefaultPropDescFactory(
            dialect::quote,
            namingStrategy
        )
    }
    open val dataDescFactory: DataDescFactory by lazy {
        DefaultDataDescFactory(
            metadataResolver,
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
    open val entitySqlBuilder: EntitySqlBuilder by lazy {
        DefaultEntitySqlBuilder(
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
    open val sqlRewriter: SqlRewriter by lazy { DefaultSqlRewriter() }
    open val sqlBuilder: SqlBuilder by lazy {
        DefaultSqlBuilder(
            dialect::formatValue,
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
    private val transactionManagerDelegate = lazy { TransactionManager(dataSource, logger) }
    val transactionManager: TransactionManager by transactionManagerDelegate
    val transactionScope: TransactionScope
        get() = TransactionScope(transactionManager, isolationLevel)
    val connection: Connection
        get() = if (transactionManagerDelegate.isInitialized())
            transactionManager.getDataSource().connection
        else
            dataSource.connection
}
