package org.komapper.core

import java.sql.SQLException

class OptimisticLockException : Exception()

class UniqueConstraintException(cause: SQLException) : Exception(cause)
