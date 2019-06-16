package org.komapper.meta

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.Embedded
import org.komapper.Id

internal class DefaultEntityMetaFactoryTest {

    private val namingStrategy = CamelToSnake()

    private val factory = DefaultEntityMetaFactory(
        namingStrategy,
        DefaultPropMetaFactory(namingStrategy, DefaultEmbeddedMetaFactory())
    )

    private data class Person(@Embedded val nested: Person)

    private data class EmployeeInfo(@Embedded val manager: Employee)

    private data class Employee(@Embedded val info: EmployeeInfo)

    private data class AddressInfo(@Id val id: Int)

    private data class Address(@Embedded val info: AddressInfo)

    @Test
    fun get_directCircularReference() {
        val exception = assertThrows<IllegalStateException> { factory.get(Person::class) }
        println(exception)
    }

    @Test
    fun get_indirectCircularReference() {
        val exception = assertThrows<IllegalStateException> { factory.get(Employee::class) }
        println(exception)
    }

}
