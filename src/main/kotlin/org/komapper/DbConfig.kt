package org.komapper

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
import javax.sql.DataSource

data class DbConfig(
    val name: String = "",
    private val dataSource: DataSource,
    val dialect: Dialect,
    val namingStrategy: NamingStrategy = CamelToSnake(),
    val objectMetaFactory: ObjectMetaFactory = DefaultObjectMetaFactory(),
    val propMetaFactory: PropMetaFactory = DefaultPropMetaFactory(namingStrategy),
    val entityMetaFactory: EntityMetaFactory = DefaultEntityMetaFactory(namingStrategy, propMetaFactory),
    val listener: EntityListener = DefaultEntityListener(),
    val queryBuilder: QueryBuilder = DefaultQueryBuilder(dialect),
    val exprNodeFactory: ExprNodeFactory = CacheExprNodeFactory(),
    val exprEvaluator: ExprEvaluator = DefaultExprEvaluator(exprNodeFactory),
    val sqlNodeFactory: SqlNodeFactory = CacheSqlNodeFactory(),
    val sqlBuilder: SqlBuilder = DefaultSqlBuilder(dialect::formatValue, sqlNodeFactory, exprEvaluator),
    val logger: Logger = {},
    val useTransaction: Boolean = false,
    val isolationLevel: TransactionIsolationLevel? = null,
    val batchSize: Int = 10,
    val fetchSize: Int? = null,
    val maxRows: Int? = null,
    val queryTimeout: Int? = null
) {

    private val transactionManager: TransactionManager by lazy {
        check(useTransaction)
        TransactionManager(dataSource, logger)
    }

    val transactionScope: TransactionScope by lazy {
        if (useTransaction) {
            TransactionScope(transactionManager, isolationLevel)
        } else {
            throw DbConfigException("To use transaction, specify \"useTransaction = true\" at DbConfig.")
        }
    }

    val connectionProvider: DataSource =
        if (useTransaction)
            transactionManager.getDataSource()
        else
            dataSource
}
