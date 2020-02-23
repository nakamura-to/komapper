package org.komapper.core.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DefaultEntityDescFactoryTest {

    data class Person(val nested: Person)
    data class EmployeeInfo(val manager: Employee)
    data class Employee(val info: EmployeeInfo)
    data class AddressInfo(val id: Int)
    data class Address(val info: AddressInfo)

    private val metadata = entities {
        entity(Person::class) {
            embedded(Person::nested)
        }
        entity(EmployeeInfo::class) {
            embedded(EmployeeInfo::manager)
        }
        entity(Employee::class) {
            embedded(Employee::info)
        }
        entity(AddressInfo::class) {
            id(AddressInfo::id)
        }
        entity(Address::class) {
            id(Address::info)
        }
    }

    private val namingStrategy = CamelToSnake()

    private val dataDescFactory = DefaultDataDescFactory(
        DefaultEntityMetaResolver(metadata),
        DefaultPropDescFactory(
            { it },
            namingStrategy
        )
    )

    private val factory = DefaultEntityDescFactory(
        dataDescFactory,
        { it },
        namingStrategy
    )

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
