package koma

import java.sql.SQLException

class DbConfigException(message: String) : Exception(message)

class OptimisticLockException : Exception()

class UniqueConstraintException(cause: SQLException) : Exception(cause)
