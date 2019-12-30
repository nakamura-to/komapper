package org.komapper.core.meta2

import java.time.LocalDateTime
import org.junit.jupiter.api.Test

internal class MetaFactoryTest {

    data class Address(
        val id: Int,
        val name: String,
        val version: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    object AddressMeta : Meta<Address>({
        entity {
            id {
                generate(Address::id)
            }
            version(Address::version)
            createdAt(Address::createdAt)
            updatedAt(Address::updatedAt)
        }

        table {
            name(Address::class)
            column(Address::id, name = "address_id", quote = true)
        }
    })

    @Test
    fun test() {
        val f = MetaFactory()
        val m = f.get(Address::class)
        println(m.metadata.table)
    }
}
