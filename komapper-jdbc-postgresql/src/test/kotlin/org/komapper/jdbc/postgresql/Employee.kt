package org.komapper.jdbc.postgresql

import java.math.BigDecimal
import java.time.LocalDate
import org.komapper.core.metadata.EntityMetadata

data class Employee(
    val employeeId: Int,
    val employeeNo: Int,
    val employeeName: String,
    val managerId: Int?,
    val hiredate: LocalDate,
    val salary: BigDecimal,
    val departmentId: Int,
    val addressId: Int,
    val version: Int
)

object EmployeeMetadata : EntityMetadata<Employee>({
    id(Employee::employeeId)
    version(Employee::version)
})
