package org.komapper.it

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.Db

@ExtendWith(Env::class)
class InsertTest {

    @Test
    fun test(db: Db) {
        db.transaction {
            val address = Address(100, "a", 0)
            db.insert(address)
        }
    }

}
