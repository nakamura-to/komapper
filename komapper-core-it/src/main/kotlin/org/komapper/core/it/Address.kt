package org.komapper.core.it

import org.komapper.core.Id
import org.komapper.core.Version

data class Address(
    @Id
    val addressId: Int = 0,
    val street: String,
    @Version
    val version: Int = 0
)
