package org.komapper

import org.komapper.core.Logger
import org.komapper.core.StdoutLogger
import org.komapper.expr.CacheExprNodeFactory
import org.komapper.expr.DefaultExprEvaluator
import org.komapper.expr.ExprEvaluator
import org.komapper.expr.ExprNodeFactory
import org.komapper.jdbc.Dialect
import org.komapper.meta.*
import org.komapper.query.DefaultQueryBuilder
import org.komapper.query.QueryBuilder
import org.komapper.sql.CacheSqlNodeFactory
import org.komapper.sql.DefaultSqlBuilder
import org.komapper.sql.SqlBuilder
import org.komapper.sql.SqlNodeFactory
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
    val propMetaFactory: PropMetaFactory = DefaultPropMetaFactory(namingStrategy, embeddedMetaFactory),
    val entityMetaFactory: EntityMetaFactory = DefaultEntityMetaFactory(namingStrategy, propMetaFactory),
    val listener: EntityListener = DefaultEntityListener(),
    val queryBuilder: QueryBuilder = DefaultQueryBuilder(dialect),
    val exprNodeFactory: ExprNodeFactory = CacheExprNodeFactory(),
    val exprEvaluator: ExprEvaluator = DefaultExprEvaluator(exprNodeFactory),
    val sqlNodeFactory: SqlNodeFactory = CacheSqlNodeFactory(),
    val sqlBuilder: SqlBuilder = DefaultSqlBuilder(dialect::formatValue, sqlNodeFactory, exprEvaluator),
    val logger: Logger = StdoutLogger(),
    val useTransaction: Boolean = false,
    val isolationLevel: TransactionIsolationLevel? = null,
    val batchSize: Int = 10,
    val fetchSize: Int? = null,
    val maxRows: Int? = null,
    val queryTimeout: Int? = null
) {

    private val transactionManager: TransactionManager by lazy {
        TransactionManager(dataSource, logger)
    }

    val transactionScope: TransactionScope by lazy {
        if (useTransaction) {
            TransactionScope(transactionManager, isolationLevel)
        } else {
            error("To use transaction, specify \"useTransaction = true\" at DbConfig.")
        }
    }

    val connection: Connection
        get() = if (useTransaction)
            transactionManager.getDataSource().connection
        else
            dataSource.connection
}

