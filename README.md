Komapper: Kotlin SQL Mapper
===========================

[![Build Status](https://travis-ci.org/nakamura-to/komapper.svg?branch=master)](https://travis-ci.org/nakamura-to/komapper)
[ ![Download](https://api.bintray.com/packages/nakamura-to/maven/komapper/images/download.svg) ](https://bintray.com/nakamura-to/maven/komapper/_latestVersion)

Komapper is a prototype for a simple database access library for Kotlin.

Supported databases are as follows:

- H2 1.4.199 or above
- PostgreSQL 10 or above 

## Getting Started

In this example, we use H2 Database Engine.

### Gradle

```groovy
repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.komapper:komapper-jdbc-h2:0.1.6")
    runtime("com.h2database:h2:1.4.199")
}
```

### Kotlin Code

```kotlin
package example

import java.time.LocalDateTime
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.metadata.EntityMetadata
import org.komapper.core.metadata.SequenceGenerator
import org.komapper.jdbc.h2.H2Dialect

// entity
data class Address(
    val id: Int = 0,
    val street: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val version: Int = 0
)

// entity metadata
object AddressMetadata : EntityMetadata<Address>({
    id(Address::id, SequenceGenerator("ADDRESS_SEQ", 100))
    createdAt(Address::createdAt)
    updatedAt(Address::updatedAt)
    version(Address::version)
    table {
        column(Address::id, "address_id")
    }
})

fun main() {
    val db = Db(
        object : DbConfig() {
            // dataSource for H2
            override val dataSource = SimpleDataSource("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
            // dialect for H2
            override val dialect = H2Dialect()
        }
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

    db.transaction {
        // insert into address (address_id, street, created_at, updated_at, version) values (1, 'street A', '2019-09-07T17:24:25.729', null, 0)
        val addressA = db.insert(Address(street = "street A"))

        // Address(id=1, street=street A, createdAt=2019-09-07T17:24:25.729, updatedAt=null, version=0)
        println(addressA)

        // select address_id, street, created_at, updated_at, version from address where address_id = 1
        val foundA = db.findById<Address>(1)

        assert(addressA == foundA)

        // update address set street = 'street B', created_at = '2019-09-07T17:24:25.729', updated_at = '2019-09-07T17:24:25.816', version = 1 where address_id = 1 and version = 0
        val addressB = db.update(addressA.copy(street = "street B"))

        // Address(id=1, street=street B, createdAt=2019-09-07T17:24:25.729, updatedAt=2019-09-07T17:24:25.816, version=1)
        println(addressB)

        // select t0_.address_id, t0_.street, t0_.created_at, t0_.updated_at, t0_.version from address t0_ where t0_.street = 'street B'
        val foundB1 = db.select<Address> {
            where {
                Address::street eq "street B"
            }
        }.first()

        // select address_id, street, created_at, updated_at, version from Address where street = 'street B'
        val foundB2 = db.query<Address>(
            "select /*%expand*/* from Address where street = /*street*/'test'",
            object {
                val street = "street B"
            }
        ).first()

        assert(addressB == foundB1 && foundB1 == foundB2)

        // delete from address where address_id = 1 and version = 1
        db.delete(addressB)

        // select t0_.address_id, t0_.street, t0_.created_at, t0_.updated_at, t0_.version from address t0_
        val addressList = db.select<Address>()

        assert(addressList.isEmpty())
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
