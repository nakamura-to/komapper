package org.komapper.jdbc.postgresql

data class Address(
    val addressId: Int = 0,
    val street: String,
    val version: Int = 0
)
