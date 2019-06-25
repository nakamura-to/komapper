package org.komapper.it

import org.komapper.Id
import org.komapper.Version

data class Address(
    @Id
    val addressId: Int = 0,
    val street: String,
    @Version
    val version: Int = 0
)
