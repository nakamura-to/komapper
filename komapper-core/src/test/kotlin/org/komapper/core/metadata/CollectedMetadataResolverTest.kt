package org.komapper.core.metadata

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class CollectedMetadataResolverTest {

    data class Address(
        val id: Int,
        val name: String,
        val version: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    private val addressMeta = entity(Address::class) {
        id(Address::id, SequenceGenerator("address_seq"))
        version(Address::version)
        createdAt(Address::createdAt)
        updatedAt(Address::updatedAt)
        table {
            name("ADDRESS")
            column(Address::id, name = "address_id", quote = true)
        }
    }

    data class Person(
        val id: Int,
        val name: String,
        val version: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    @Test
    fun testRegistered() {
        val resolver = CollectedMetadataResolver(addressMeta)
        assertEquals(addressMeta, resolver.resolve(Address::class))
    }

    @Test
    fun testNotRegistered() {
        val resolver = CollectedMetadataResolver(addressMeta)
        assertDoesNotThrow {
            resolver.resolve(Person::class)
        }
    }
}
