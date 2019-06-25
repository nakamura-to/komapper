package org.komapper.it

import org.komapper.Id
import org.komapper.Version
import java.math.BigDecimal
import java.time.LocalDate

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
