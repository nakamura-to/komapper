# Template Queries

## Select
Create a query and pass it to the `select` function of the Db instance:
```kotlin
val query = template<Address>(
    "select /*%expand*/* from address where street = /*street*/'dummy data'",
    object {
        val street = "street A"
    }
)

val addressList = db.select(query)
```

The above query is converted to the SQL as follows:

```sql
select
  address_id,
  street,
  version
from
  address
where
  street = ?
```

## Insert, Update, Delete

TODO
