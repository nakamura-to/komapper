package org.komapper.core.criteria

import java.sql.SQLException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.builder.SelectBuilder
import org.komapper.core.desc.CamelToSnake
import org.komapper.core.desc.DefaultDataDescFactory
import org.komapper.core.desc.DefaultEntityDescFactory
import org.komapper.core.desc.DefaultPropDescFactory
import org.komapper.core.jdbc.AbstractDialect
import org.komapper.core.meta.DefaultEntityMetaResolver
import org.komapper.core.meta.entities

internal class SelectBuilderTest {
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
        val criteria = SelectCriteria(Address::class).apply {
            val alias = this.alias
            where.add(Criterion.Eq(alias[Address::street], Expression.wrap("a")))
        }
        val processor = SelectBuilder(MyDialect(), factory, criteria)
        val sql = processor.build()
        assertEquals("select t0_.id, t0_.street from address t0_ where t0_.street = ?", sql.text)
    }
}
