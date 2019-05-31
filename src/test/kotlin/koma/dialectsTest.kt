package koma

import koma.jdbc.EnumType
import koma.jdbc.JdbcType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.SQLException
import kotlin.reflect.KClass

internal class AbstractDialectTest {

    class MyDialect : AbstractDialect() {

        public override fun getJdbcType(type: KClass<*>): JdbcType<*> {
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
        assertTrue(EnumType::class.isInstance(jdbcType))
    }
}
