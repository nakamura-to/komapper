package org.komapper.core.it

import org.komapper.core.metadata.EntityMetadata

data class Address(
    val addressId: Int = 0,
    val street: String,
    val version: Int = 0
)

object AddressMetadata : EntityMetadata<Address>({
    id(Address::addressId)
    version(Address::version)
})
