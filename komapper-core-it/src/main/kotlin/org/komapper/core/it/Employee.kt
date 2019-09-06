package org.komapper.core.it

import java.math.BigDecimal
import java.time.LocalDate
import org.komapper.core.Id
import org.komapper.core.Version

data class Employee(
    @Id
    val employeeId: Int,
    val employeeNo: Int,
    val employeeName: String,
    val managerId: Int?,
    val hiredate: LocalDate,
    val salary: BigDecimal,
    val departmentId: Int,
    val addressId: Int,
    @Version
    val version: Int
)
