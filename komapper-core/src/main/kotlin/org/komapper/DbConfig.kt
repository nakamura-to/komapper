package org.komapper

import org.komapper.expr.CacheExprNodeFactory
import org.komapper.expr.DefaultExprEvaluator
import org.komapper.expr.ExprEvaluator
import org.komapper.expr.ExprNodeFactory
import org.komapper.jdbc.Dialect
import org.komapper.logging.Logger
import org.komapper.logging.StdoutLogger
import org.komapper.meta.*
import org.komapper.sql.*
import org.komapper.tx.TransactionIsolationLevel
import org.komapper.tx.TransactionManager
import org.komapper.tx.TransactionScope
import java.sql.Connection
import javax.sql.DataSource

data class DbConfig(
    val name: String = System.identityHashCode(object {}).toString(),
    val dataSource: DataSource,
    val dialect: Dialect,
    val namingStrategy: NamingStrategy = CamelToSnake(),
    val objectMetaFactory: ObjectMetaFactory = DefaultObjectMetaFactory(),
    val embeddedMetaFactory: EmbeddedMetaFactory = DefaultEmbeddedMetaFactory(),
    val propMetaFactory: PropMetaFactory = DefaultPropMetaFactory(dialect::quote, namingStrategy, embeddedMetaFactory),
    val entityMetaFactory: EntityMetaFactory = DefaultEntityMetaFactory(
        dialect::quote,
        namingStrategy,
        propMetaFactory
    ),
    val listener: EntityListener = DefaultEntityListener(),
    val entitySqlBuilder: EntitySqlBuilder = DefaultEntitySqlBuilder(dialect),
    val exprNodeFactory: ExprNodeFactory = CacheExprNodeFactory(),
    val exprEvaluator: ExprEvaluator = DefaultExprEvaluator(exprNodeFactory),
    val sqlNodeFactory: SqlNodeFactory = CacheSqlNodeFactory(),
    val sqlRewriter: SqlRewriter = DefaultSqlRewriter(sqlNodeFactory),
    val sqlBuilder: SqlBuilder = DefaultSqlBuilder(dialect::formatValue, sqlNodeFactory, exprEvaluator),
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

