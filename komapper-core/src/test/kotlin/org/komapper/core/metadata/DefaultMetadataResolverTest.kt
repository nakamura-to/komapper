package org.komapper.core.metadata

import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class DefaultMetadataResolverTest {

    data class Address(
        val id: Int,
        val name: String,
        val version: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    object AddressMetadata : EntityMetadata<Address>({
        id(Address::id, SequenceGenerator("address_seq"))
        version(Address::version)
        createdAt(Address::createdAt)
        updatedAt(Address::updatedAt)

        table {
            name("ADDRESS")
            column(Address::id, name = "address_id", quote = true)
        }
    })

    data class Person(
        val id: Int,
        val name: String,
        val version: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    class PersonMetadata : EntityMetadata<Person>({
        id(Person::id, SequenceGenerator("person_seq"))
        version(Person::version)
        createdAt(Person::createdAt)
        updatedAt(Person::updatedAt)

        table {
            name("PERSON")
            column(Person::id, name = "person_id", quote = true)
        }
    })

    @Test
    fun testObject() {
        val resolver = DefaultMetadataResolver()
        assertDoesNotThrow {
            resolver.resolve(Address::class)
        }
    }

    @Test
    fun testClass() {
        val resolver = DefaultMetadataResolver()
        assertDoesNotThrow {
            resolver.resolve(Person::class)
        }
    }
}
