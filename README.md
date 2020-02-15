Komapper: Kotlin SQL Mapper
===========================

[![Build Status](https://travis-ci.org/nakamura-to/komapper.svg?branch=master)](https://travis-ci.org/nakamura-to/komapper)
[ ![Download](https://api.bintray.com/packages/nakamura-to/maven/komapper-core/images/download.svg) ](https://bintray.com/nakamura-to/maven/komapper-core/_latestVersion)

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
    implementation("org.komapper:komapper-jdbc-h2:0.1.7")
}
```

### Kotlin Code

```kotlin
package example

import java.time.LocalDateTime
import org.komapper.core.Db
import org.komapper.core.DbConfig
import org.komapper.core.criteria.select
import org.komapper.core.jdbc.SimpleDataSource
import org.komapper.core.meta.CollectedMetadataResolver
import org.komapper.core.meta.SequenceGenerator
import org.komapper.core.meta.entities
import org.komapper.core.sql.template
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
val metadata = entities {
    entity<Address> {
        id(Address::id, SequenceGenerator("ADDRESS_SEQ", 100))
        createdAt(Address::createdAt)
        updatedAt(Address::updatedAt)
        version(Address::version)
        table {
            column(Address::id, "address_id")
        }
    }
}

fun main() {
    // create Db instance
    val db = Db(
        object : DbConfig() {
            // dataSource for H2
            override val dataSource = SimpleDataSource("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
            // dialect for H2
            override val dialect = H2Dialect()
            // register entity metadata
            override val metadataResolver = CollectedMetadataResolver(metadata)
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

    // execute simple CRUD operations as a transaction
    db.transaction {
        // CREATE
        val addressA = db.insert(Address(street = "street A"))
        println(addressA)

        // READ: select by identifier
        val foundA = db.findById<Address>(1)
        assert(addressA == foundA)

        // UPDATE
        val addressB = db.update(addressA.copy(street = "street B"))
        println(addressB)

        // READ: select by criteria query
        val criteriaQuery = select<Address> {
            where {
                eq(Address::street, "street B")
            }
        }
        val foundB1 = db.select(criteriaQuery).first()
        assert(addressB == foundB1)

        // READ: select by template query
        val templateQuery = template<Address>(
            "select /*%expand*/* from Address where street = /*street*/'test'",
            object {
                val street = "street B"
            }
        )
        val foundB2 = db.select(templateQuery).first()
        assert(addressB == foundB2)

        // DELETE
        db.delete(addressB)

        // READ: select by criteria query
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
