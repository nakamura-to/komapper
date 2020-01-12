package org.komapper.jdbc.postgresql

data class Department(
    val departmentId: Int,
    val departmentNo: Int,
    val departmentName: String,
    val Location: String,
    val version: Int
)
