package org.komapper.jdbc.postgresql

import java.sql.SQLException
import org.komapper.core.jdbc.AbstractDialect

open class PostgreSqlDialect(val version: Version = Version.V10) : AbstractDialect() {

    companion object {
        enum class Version { V10 }

        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.sqlState == UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "select nextval('$sequenceName')"
    }

    override fun supportsMerge(): Boolean = false

    override fun supportsUpsert(): Boolean = true
}
