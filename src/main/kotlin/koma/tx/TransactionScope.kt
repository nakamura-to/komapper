package koma.tx


class TransactionScope(
    private val transactionManager: TransactionManager,
    private val defaultIsolationLevel: TransactionIsolationLevel? = null
) {

    fun <R> required(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
        block: TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            block(this)
        } else {
            executeInTransaction(isolationLevel, block)
        }
    }

    fun <R> requiresNew(
        isolationLevel: TransactionIsolationLevel? = defaultIsolationLevel,
        block: TransactionScope.() -> R
    ): R {
        return if (transactionManager.isActive) {
            val context = transactionManager.suspend()
            try {
                executeInTransaction(isolationLevel, block)
            } finally {
                transactionManager.resume(context)
            }
        } else {
            executeInTransaction(isolationLevel, block)
        }
    }

    private fun <R> executeInTransaction(
        isolationLevel: TransactionIsolationLevel?, block: TransactionScope.() -> R
    ): R {
        transactionManager.begin(isolationLevel)
        try {
            val result = block(this)
            if (!transactionManager.isRollbackOnly) {
                transactionManager.commit()
            }
            return result
        } finally {
            transactionManager.rollback()
        }
    }

    fun setRollbackOnly() {
        transactionManager.setRollbackOnly()
    }

    fun isRollbackOnly(): Boolean {
        return transactionManager.isRollbackOnly
    }
}