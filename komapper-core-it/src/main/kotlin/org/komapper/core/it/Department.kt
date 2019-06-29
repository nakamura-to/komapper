package org.komapper.core.it

import org.komapper.core.Id
import org.komapper.core.Version

data class Department(
    @Id
    val departmentId: Int,
    val departmentNo: Int,
    val departmentName: String,
    val Location: String,
    @Version
    val version: Int
)
