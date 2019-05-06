Koma: Two-way-SQL parser for Kotlin
===================================

## Example

```kotlin
val template = "select name, age from person where name = /*name*/'test' and age > 1"
val sql = SqlBuilder().build(template, mapOf("name" to "aaa"))
assertEquals("select name, age from person where name = ? and age > 1", sql.text)
assertEquals(listOf("aaa"), sql.values)
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
