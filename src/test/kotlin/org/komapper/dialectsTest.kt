package org.komapper

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.SQLException
import kotlin.reflect.KClass

internal class AbstractDialectTest {

    class MyDialect : org.komapper.AbstractDialect() {

        public override fun getJdbcType(type: KClass<*>): org.komapper.jdbc.JdbcType<*> {
            return super.getJdbcType(type)
        }

        override fun getSequenceSql(sequenceName: String): String {
            throw UnsupportedOperationException()
        }

        override fun isUniqueConstraintViolated(exception: SQLException): Boolean {
            throw UnsupportedOperationException()
        }
    }

    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    private val dialect = MyDialect()

    @Test
    fun getJdbcType() {
        val jdbcType = dialect.getJdbcType(Direction::class)
        assertTrue(org.komapper.jdbc.EnumType::class.isInstance(jdbcType))
    }
}
