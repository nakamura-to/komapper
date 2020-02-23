package org.komapper.core.builder

import java.sql.SQLException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.criteria.Criterion
import org.komapper.core.criteria.DeleteCriteria
import org.komapper.core.criteria.Expression
import org.komapper.core.entity.CamelToSnake
import org.komapper.core.entity.DefaultDataDescFactory
import org.komapper.core.entity.DefaultEntityDescFactory
import org.komapper.core.entity.DefaultEntityMetaResolver
import org.komapper.core.entity.DefaultPropDescFactory
import org.komapper.core.entity.entities
import org.komapper.core.jdbc.AbstractDialect

internal class DeleteBuilderTest {
    private data class Address(
        val id: Int,
        val street: String
    )

    private val metadata = entities {
        entity(Address::class) {
            id(Address::id)
        }
    }

    private class MyDialect : AbstractDialect() {
        override fun isUniqueConstraintViolation(exception: SQLException): Boolean = false
        override fun getSequenceSql(sequenceName: String): String = ""
    }

    private val namingStrategy = CamelToSnake()

    private val dataDescFactory = DefaultDataDescFactory(
        DefaultEntityMetaResolver(metadata),
        DefaultPropDescFactory(
            { it },
            namingStrategy
        )
    )

    private val factory = DefaultEntityDescFactory(
        dataDescFactory,
        { it },
        namingStrategy
    )

    @Test
    fun test() {
        val criteria = DeleteCriteria(Address::class)
            .apply {
            val alias = this.alias
            where.add(
                Criterion.Eq(
                    alias[Address::street],
                    Expression.wrap("a")
                )
            )
        }
        val builder = DeleteBuilder(MyDialect(), factory, criteria)
        val stmt = builder.build()
        assertEquals("delete from address t0_ where t0_.street = ?", stmt.sql)
    }
}
