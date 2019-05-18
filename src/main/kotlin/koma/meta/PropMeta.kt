package koma.meta

import kotlin.reflect.KParameter

data class PropMeta(val kParameter: KParameter) {
    val columnName: String = kParameter.name!!
}

fun makePropMeta(kParameter: KParameter): PropMeta {
    return PropMeta(kParameter)
}