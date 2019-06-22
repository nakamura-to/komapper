Komapper: Kotlin SQL Mapper
===========================

[![Build Status](https://travis-ci.org/nakamura-to/komapper.svg?branch=master)](https://travis-ci.org/nakamura-to/komapper)
[ ![Download](https://api.bintray.com/packages/nakamura-to/maven/komapper/images/download.svg) ](https://bintray.com/nakamura-to/maven/komapper/_latestVersion)

Komapper is a prototype for a simple database access library for Kotlin.
Currently supported database is H2 Database only.

## Getting Started

### Gradle

```groovy
repositories {
  mavenCentral()
  jcenter()
}

dependencies {
  implementation 'org.komapper:komapper:0.1.2'
  runtime 'com.h2database:h2:1.4.199'
}
```

### Example

```kotlin
package example

import org.komapper.*
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
        // insert into address (address_id, street, created_at, updated_at, version) values(1, 'street A', '2019-06-02 18:15:36.561', null, 0)
        val addressA = db.insert(Address(street = "street A"))

        // Address(id=1, street=street A, createdAt=2019-06-02T18:15:36.561, updatedAt=null, version=0)
        println(addressA)

        // select address_id, street, created_at, updated_at, version from address where address_id = 1
        val foundA = db.findById<Address>(1)

        // true
        println(addressA == foundA)

        // update address set street = 'street B', created_at = '2019-06-02 18:15:36.561', updated_at = '2019-06-02 18:15:36.601', version = 1 where address_id = 1 and version = 0
        val addressB = db.update(addressA.copy(street = "street B"))

        // Address(id=1, street=street B, createdAt=2019-06-02T18:15:36.561, updatedAt=2019-06-02T18:15:36.601, version=1)
        println(addressB)

        // select address_id, street, created_at, updated_at, version from address where street = 'street B'
        val foundB = db.query<Address> {
            where {
                Address::street eq "street B"
            }
        }.first()

        // true
        println(addressB == foundB)

        // delete from address where address_id = 1 and version = 1
        db.delete(addressB)

        // select address_id, street, created_at, updated_at, version from address
        val addressList = db.query<Address>()

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
