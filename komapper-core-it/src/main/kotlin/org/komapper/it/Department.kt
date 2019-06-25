package org.komapper.it

import org.komapper.Id
import org.komapper.Version

data class Department(
    @Id
    val departmentId: Int,
    val departmentNo: Int,
    val departmentName: String,
    val Location: String,
    @Version
    val version: Int
)
