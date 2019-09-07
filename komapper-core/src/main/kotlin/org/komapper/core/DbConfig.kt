package org.komapper.core

import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource
import org.komapper.core.expr.CacheExprNodeFactory
import org.komapper.core.expr.DefaultExprEnvironment
import org.komapper.core.expr.DefaultExprEvaluator
import org.komapper.core.expr.ExprEnvironment
import org.komapper.core.expr.ExprEvaluator
import org.komapper.core.expr.ExprNodeFactory
import org.komapper.core.jdbc.Dialect
import org.komapper.core.logging.Logger
import org.komapper.core.logging.StdoutLogger
import org.komapper.core.meta.CamelToSnake
import org.komapper.core.meta.DefaultEmbeddedMetaFactory
import org.komapper.core.meta.DefaultEntityListener
import org.komapper.core.meta.DefaultEntityMetaFactory
import org.komapper.core.meta.DefaultEntitySqlBuilder
import org.komapper.core.meta.DefaultObjectMetaFactory
import org.komapper.core.meta.DefaultPropMetaFactory
import org.komapper.core.meta.EmbeddedMetaFactory
import org.komapper.core.meta.EntityListener
import org.komapper.core.meta.EntityMetaFactory
import org.komapper.core.meta.EntitySqlBuilder
import org.komapper.core.meta.NamingStrategy
import org.komapper.core.meta.ObjectMetaFactory
import org.komapper.core.meta.PropMetaFactory
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
 * @property name the key which is used to manage sequence values. The name must be unique.
 * @property dataSource the data source
 * @property dialect the dialect
 * @property namingStrategy the naming strategy for entity classes and properties.
 * @property objectMetaFactory the object meta factory
 * @property embeddedMetaFactory the embedded meta factory
 * @property propMetaFactory the property meta factory
 * @property entityMetaFactory the entity meta factory
 * @property listener the entity listener
 * @property entitySqlBuilder the sql builder for entities
 * @property exprNodeFactory the expression node factory
 * @property exprEnvironment the expression environment
 * @property exprEvaluator the expression evaluator
 * @property sqlNodeFactory the sql node factory
 * @property sqlRewriter the sql rewriter
 * @property sqlBuilder the sql builder
 * @property logger the logger
 * @property useTransaction whether the built-in transaction is used or not
 * @property isolationLevel the isolation level. This value is used only when [useTransaction] is true.
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size. See [PreparedStatement.setFetchSize].
 * @property maxRows the max rows. See [PreparedStatement.setMaxRows].
 * @property queryTimeout the query timeout. See [PreparedStatement.setQueryTimeout].
 */
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
