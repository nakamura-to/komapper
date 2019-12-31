package org.komapper.core.desc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.komapper.core.metadata.DefaultMetadataResolver
import org.komapper.core.metadata.EntityMetadata

internal class DefaultEntityDescFactoryTest {

    private val metadataResolver = DefaultMetadataResolver()

    private val namingStrategy = CamelToSnake()

    private val dataDescFactory = DefaultDataDescFactory(
        metadataResolver,
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

    data class Person(val nested: Person)
    object PersonMetadata : EntityMetadata<Person>({
        embedded(Person::nested)
    })

    data class EmployeeInfo(val manager: Employee)
    object EmployeeInfoMetadata : EntityMetadata<EmployeeInfo>({
        embedded(EmployeeInfo::manager)
    })

    data class Employee(val info: EmployeeInfo)
    object EmployeeMetadata : EntityMetadata<Employee>({
        embedded(Employee::info)
    })

    data class AddressInfo(val id: Int)
    object AddressInfoMetadata : EntityMetadata<AddressInfo>({
        id(AddressInfo::id)
    })

    data class Address(val info: AddressInfo)
    object AddressMetadata : EntityMetadata<Address>({
        id(Address::info)
    })

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
