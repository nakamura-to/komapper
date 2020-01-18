package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.desc.EntityDesc
import org.komapper.core.desc.EntityListener
import org.komapper.core.desc.GlobalEntityListener

@ExtendWith(Env::class)
internal class BatchDeleteTest(private val db: Db) {

    @Test
    fun test() {
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        val sql = "select * from address where address_id in (16, 17, 18)"
        Assertions.assertEquals(
            addressList,
            db.query<Address>(sql)
        )
        db.batchDelete(addressList)
        Assertions.assertTrue(
            db.query<Address>(
                sql
            ).isEmpty()
        )
    }

    @Test
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val metadataResolver = db.config.metadataResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preDelete(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postDelete(
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

        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        val sql = "select * from address where address_id in (16, 17, 18)"
        Assertions.assertEquals(
            addressList,
            db.query<Address>(sql)
        )
        val list = db.batchDelete(addressList)
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16*", 0),
                Address(17, "*STREET 17*", 0),
                Address(18, "*STREET 18*", 0)
            ), list
        )
        Assertions.assertTrue(
            db.query<Address>(
                sql
            ).isEmpty()
        )
    }

    @Test
    fun entityListener() {
        val db = Db(
            AddressListenerConfig(
                db.config,
                object :
                    EntityListener<Address> {
                    override fun preDelete(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postDelete(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "${entity.street}*")
                    }
                })
        )

        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        val sql = "select * from address where address_id in (16, 17, 18)"
        Assertions.assertEquals(
            addressList,
            db.query<Address>(sql)
        )
        val list = db.batchDelete(addressList)
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16*", 0),
                Address(17, "*STREET 17*", 0),
                Address(18, "*STREET 18*", 0)
            ), list
        )
        Assertions.assertTrue(
            db.query<Address>(
                sql
            ).isEmpty()
        )
    }

    @Test
    fun optimisticLockException() {
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        assertThrows<OptimisticLockException> {
            db.batchDelete(
                listOf(
                    addressList[0],
                    addressList[1],
                    addressList[2].copy(version = +1)
                )
            )
        }
    }
}
