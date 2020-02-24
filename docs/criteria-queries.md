# Criteria Queries

## Select
Create a query and pass it to the `select` function of the Db instance:
```kotlin
val query = select<Address> {
    where {
        eq(Address::street, "street A")
    }
}

val addressList = db.select(query)
```

Or use the shortcut way as follows:
```kotlin
val addressList = db.select<Address> {
    where {
        eq(Address::street, "street A")
    }
}
```

The above query is converted to the SQL as follows:

```sql
select
  t0_.address_id,
  t0_.street,
  t0_.version
from
  address t0_
where
  t0_.street = ?
```

### Sort
```kotlin
select<Address> {
    where {
        eq(Address::street, "street A")
    }
    orderBy {
        asc(Address::id)
        desc(Address::version)
    }
}
```

The above query is converted to the SQL as follows:

```sql
select
  t0_.address_id,
  t0_.street,
  t0_.version
from
  address t0_
where
  t0_.street = ?
order by
  t0_.address_id asc,
  t0_.version desc
```

### Paging
```kotlin
select<Address> {
    limit(3)
    offset(5)
}
```

The above query is converted to the SQL as follows:

```sql
select
    t0_.address_id,
    t0_.street,
    t0_.version
from
    address t0_
limit 3
offset 5
```

### Join

#### Inner join
```kotlin
select<Employee> { e ->
    innerJoin<Address> { a ->
        eq(e[Employee::addressId], a[Address::addressId])
    }
}
```

Use special notations to specify properties as follows:

```kotlin
e[Employee::addressId]
``` 
```kotlin
a[Address::addressId]
``` 

The `e` is the alias for the Employee entity.
And the `a` is the alias for the Address entity.

#### Left join
```kotlin
val employees = db.select<Employee> { e ->
    leftJoin<Address> { a ->
        eq(e[Employee::addressId], a[Address::addressId])
    }
}
```

#### OneToOne association
```kotlin
val map = mutableMapOf<Int, Address?>()
select<Employee> { e ->
    innerJoin<Address> { a ->
        eq(e[Employee::addressId], a[Address::addressId])
        oneToOne { employee, address -> map[employee.id] = address }
    }
}
```

#### OneToMany association
```kotlin
val map = mutableMapOf<Int, List<Employee>>()
select<Department> { d ->
    innerJoin<Address> { e ->
        eq(d[Department::departmentId], d[Employee::departmentId])
        oneToMany { department, employees -> map[department.departmentId] = employees }
    }
}
```

#### Where

```kotlin
select<Employee> { e ->
    val a = innerJoin<Address> { a ->
        eq(e[Employee::addressId], a[Address::addressId])
    }
    where {
        ge(e[Employee::age], 30)
        like(a[Address::street], "%Japan")
    }
}
```

When you use the properties of joined entities in `where` clause, 
get the alias from `innerJoin` or `leftJoin` as the return value 
and use it in the `where` clause.

## Insert
Create a query and pass it to the `insert` function of the Db instance:
```kotlin
val query = insert<Address> {
    values {
        value(Address::id, 1)
        value(Address::street, 1)
        value(Address::version, 1)
    }
}

val count = db.insert(query)
```

Or use the shortcut way as follows:
```kotlin
val count = db.insert<Address> {
    values {
        value(Address::id, 1)
        value(Address::street, "street A")
        value(Address::version, 1)
    }
}
```

## Update
Create a query and pass it to the `update` function of the Db instance:
```kotlin
val query = update<Address> {
    set {
        value(Address::street, "street B")
    }
    where {
        eq(Address::id, 1)
    }
}

val count = db.update(query)
```

Or use the shortcut way as follows:
```kotlin
val count = db.update<Address> {
    set {
        value(Address::street, "street B")
    }
    where {
        eq(Address::id, 1)
    }
}
```

## Delete
Create a query and pass it to the `delete` function of the Db instance:
```kotlin
val query = delete<Address> {
    where {
        eq(Address::id, 1)
    }
}

val count = db.delete(query)
```

Or use the shortcut way as follows:
```kotlin
val count = db.delete<Address> {
    where {
        eq(Address::id, 1)
    }
}
```


## Composition
You can compose queries:

```kotlin
val query1 = select<Address> {
    where {
        eq(Address::street, "street A")
    }
}

val query2 = select<Address> {
    where {
        ge(Address::version, 3)
    }
    orderBy {
        asc(Address::version)
    }
}

val query3 = query1 + query2
```

The above `query3` is equivalent to the following query:

```kotlin
select<Address> {
    where {
        eq(Address::street, "street A")
        ge(Address::version, 3)
    }
    orderBy {
        asc(Address::version)
    }
}
```

You can compose not only `select` clauses but also other clauses:

```kotlin
val where1: Where = {
    eq(Address::street, "street A")
}

val where2: Where = {
    ge(Address::version, 3)
}

val where3 = where1 + where2
```

You can use the `where3` query as follows:

```kotlin
select<Address> {
    where(where3)
}
```

The above query is equivalent to the following query:

```kotlin
select<Address> {
    where {
        eq(Address::street, "street A")
        ge(Address::version, 3)
    }
}
```
