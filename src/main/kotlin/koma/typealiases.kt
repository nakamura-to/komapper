package koma

import kotlin.reflect.KClass

typealias Logger = (() -> String) -> Unit

typealias Value = Pair<*, KClass<*>>
