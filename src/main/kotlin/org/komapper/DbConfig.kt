package org.komapper

import org.komapper.jdbc.Dialect
import org.komapper.meta.EntityListener
import org.komapper.meta.NamingStrategy
import org.komapper.tx.TransactionIsolationLevel
import org.komapper.tx.TransactionManager
import org.komapper.tx.TransactionScope
import javax.sql.DataSource

data class DbConfig(
    val name: String = "",
    private val dataSource: DataSource,
    val dialect: Dialect,
    val namingStrategy: NamingStrategy = object : NamingStrategy {},
    val listener: EntityListener = object : EntityListener {},
    val logger: Logger = {},
    val useTransaction: Boolean = false,
    val defaultIsolationLevel: TransactionIsolationLevel? = null,
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
            TransactionScope(transactionManager, defaultIsolationLevel)
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
