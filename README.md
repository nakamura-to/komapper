Komapper: Kotlin SQL Mapper
===========================

Komapper is a prototype for a simple database access library for Kotlin.
Currently supported database is H2 Database only.

## Example

```kotlin
package org.komapper

import org.komapper.jdbc.H2Dialect
import org.komapper.jdbc.SimpleDataSource
import java.time.LocalDateTime

data class Address(
    @Id
    @SequenceGenerator(name = "ADDRESS_SEQ", incrementBy = 100)
    @Column(name = "address_id")
    val id: Int = 0,
    val street: String,
    @CreatedAt
    val createdAt: LocalDateTime? = null,
    @UpdatedAt
    val updatedAt: LocalDateTime? = null,
    @Version
    val version: Int = 0
)

fun main() {
    val db = Db(
        DbConfig(
            dataSource = SimpleDataSource("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1"),
            dialect = H2Dialect(),
            logger = { println(it()) },
            useTransaction = true
        )
    )

    // set up schema
    db.transaction {
        db.execute(
            """
            CREATE SEQUENCE ADDRESS_SEQ START WITH 1 INCREMENT BY 100;
            CREATE TABLE ADDRESS(
                ADDRESS_ID INTEGER NOT NULL PRIMARY KEY,
                STREET VARCHAR(20) UNIQUE,
                CREATED_AT TIMESTAMP,
                UPDATED_AT TIMESTAMP,
                VERSION INTEGER
            );
            """.trimIndent()
        )
    }

    // query
    db.transaction {
        val addressA = db.insert(Address(street = "street A"))
        // Address(id=1, street=street A, createdAt=2019-06-01T22:10:28.229, updatedAt=null, version=0)
        println(addressA)

        val foundA = db.findById<Address>(1)
        // Address(id=1, street=street A, createdAt=2019-06-01T22:10:28.229, updatedAt=null, version=0)
        println(foundA)

        val addressB = db.update(addressA.copy(street = "street B"))
        // Address(id=1, street=street B, createdAt=2019-06-01T22:10:28.229, updatedAt=2019-06-01T22:10:28.291, version=1)
        println(addressB)

        val foundB = db.select<Address>("select * from address where street = /*street*/'test'", object {
            val street = "street B"
        }).first()
        // Address(id=1, street=street B, createdAt=2019-06-01T22:10:28.229, updatedAt=2019-06-01T22:10:28.291, version=1)
        println(foundB)

        db.delete(addressB)
        val addressList = db.select<Address>("select * from address")
        // 0
        println(addressList.size)
    }
}
```

## License

```
Copyright 2019 Toshihiro Nakamura

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
