package koma

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

interface Dialect {

    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        return rs.getObject(index)
    }

    fun setValue(stmt: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        stmt.setObject(index, value)
    }

    fun isUniqueConstraintViolated(exception: SQLException): Boolean

    fun getSequenceSql(sequenceName: String): String
}

abstract class AbstractDialect : Dialect {

    protected fun getErrorCode(exception: SQLException): Int {
        val cause = getCauseSQLException(exception)
        return cause.errorCode
    }

    private fun getCauseSQLException(exception: SQLException): SQLException {
        return exception.filterIsInstance(SQLException::class.java).first()
    }
}

class H2Dialect : AbstractDialect() {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
    }

    override fun isUniqueConstraintViolated(exception: SQLException): Boolean {
        val code = getErrorCode(exception)
        return UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE == code
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }
}
