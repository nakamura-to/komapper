package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.query.EntityQuery

@ExtendWith(Env::class)
class EntityUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where { a.addressId eq 15 }
        val address = db.first { query }
        val newAddress = address.copy(street = "NY street")
        db.update(a, newAddress)
        val address2 = db.firstOrNull { query }
        assertEquals(
            Address(
                15,
                "NY street",
                2
            ),
            address2
        )
    }

    @Test
    fun updatedAt() {
        val p = Person.metamodel()
        val person1 = Person(1, "ABC")
        db.insert(p, person1)
        val person2 = db.find(p) { p.personId eq 1 }
        val person3 = db.update(p, person2.copy(name = "DEF"))
        val person4 = db.find(p) { p.personId eq 1 }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        assertNotNull(person4.updatedAt)
        assertNotEquals(person2.updatedAt, person4.updatedAt)
        assertEquals(person3.updatedAt, person4.updatedAt)
    }

    /*

    @Test
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preUpdate(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postUpdate(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }
        })

        val t = template<Address>("select * from address where address_id = 15")
        val address = db.select<Address>(t).first()
        val newAddress = address.copy(street = "NY street")
        val address2 = db.update(newAddress)
        Assertions.assertEquals(
            Address(
                15,
                "*NY street*",
                2
            ), address2
        )
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                15,
                "*NY street",
                2
            ), address3
        )
    }

    @Test
    fun entityListener() {
        val db = Db(
            AddressListenerConfig(
                db.config,
                object :
                    EntityListener<Address> {
                    override fun preUpdate(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postUpdate(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "${entity.street}*")
                    }
                })
        )

        val t = template<Address>("select * from address where address_id = 15")
        val address = db.select(t).first()
        val newAddress = address.copy(street = "NY street")
        val address2 = db.update(newAddress)
        Assertions.assertEquals(
            Address(
                15,
                "*NY street*",
                2
            ), address2
        )
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                15,
                "*NY street",
                2
            ), address3
        )
    }
    
     */

    @Test
    fun uniqueConstraintException() {
        val a = Address.metamodel()
        val address = Address(1, "STREET 2", 1)
        assertThrows<UniqueConstraintException> {
            db.update(a, address)
        }
    }

    @Test
    fun optimisticLockException() {
        val a = Address.metamodel()
        val address = db.first { EntityQuery.from(a).where { a.addressId eq 15 } }
        db.update(a, address)
        assertThrows<OptimisticLockException> {
            db.update(a, address)
        }
    }

    @Test
    fun criteria() {
        val a = Address.metamodel()
        val selectQuery = EntityQuery.from(a).where { a.addressId eq 15 }
        val address1 = db.first { selectQuery }.copy(street = "new street")
        val address2 = db.update(a, address1)
        val address3 = db.first { selectQuery }
        assertEquals(Address(15, "new street", 2), address2)
        assertEquals(address2, address3)
    }

    /*
    @Test
    fun plus() {
        val query = update<Address> {
            set {
                value(Address::version, expression { Address::version + 10 })
            }
            where {
                eq(Address::addressId, 15)
            }
        }
        val count = db.update(query)
        Assertions.assertEquals(1, count)
        val address = db.findById<Address>(15)
        Assertions.assertEquals(Address(15, "STREET 15", 11), address)
    }

    @Test
    fun minus() {
        val query = update<Address> {
            set {
                value(Address::version, expression { Address::version - 10 })
            }
            where {
                eq(Address::addressId, 15)
            }
        }
        val count = db.update(query)
        Assertions.assertEquals(1, count)
        val address = db.findById<Address>(15)
        Assertions.assertEquals(Address(15, "STREET 15", -9), address)
    }

    @Test
    fun concat() {
        val query = update<Address> {
            set {
                value(Address::street, expression { "[" `||` Address::street `||` "]" })
            }
            where {
                eq(Address::addressId, 15)
            }
        }
        val count = db.update(query)
        Assertions.assertEquals(1, count)
        val address = db.findById<Address>(15)
        Assertions.assertEquals(Address(15, "[STREET 15]", 1), address)
    }
     */
}
