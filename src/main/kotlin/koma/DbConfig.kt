package koma

import koma.tx.TransactionIsolationLevel
import koma.tx.TransactionManager
import koma.tx.TransactionScope
import javax.sql.DataSource

class DbConfig(
    val name: String = "",
    dataSource: DataSource,
    val dialect: Dialect,
    val logger: Logger = {},
    val useTransaction: Boolean = false,
    val defaultIsolationLevel: TransactionIsolationLevel? = null,
    val batchSize: Int = 10
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

    val dataSource: DataSource =
        if (useTransaction)
            transactionManager.getDataSource()
        else
            dataSource
}
