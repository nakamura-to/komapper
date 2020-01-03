package org.komapper.jdbc.postgresql

import org.komapper.core.metadata.EntityMetadata

data class Department(
    val departmentId: Int,
    val departmentNo: Int,
    val departmentName: String,
    val Location: String,
    val version: Int
)

object DepartmentMetadata : EntityMetadata<Department>({
    id(Department::departmentId)
    version(Department::version)
})
