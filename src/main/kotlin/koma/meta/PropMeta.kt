package koma.meta

import koma.Column
import koma.Id
import koma.Version
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

sealed class PropKind {
    object Id : PropKind()
    object Version : PropKind()
    object Basic : PropKind()
}

data class PropMeta(
    val kParameter: KParameter,
    val kProperty: KProperty1<*, *>,
    val kind: PropKind,
    val columnName: String
) {
    fun getValue(entity: Any): Pair<Any?, KClass<*>> {
        return kProperty.call(entity) to kProperty.returnType.jvmErasure
    }
}

fun makePropMeta(kParameter: KParameter, kProperty: KProperty1<*, *>): PropMeta {
    val id = kParameter.findAnnotation<Id>()
    val version = kParameter.findAnnotation<Version>()
    val column = kParameter.findAnnotation<Column>()
    val kind = when {
        id != null && version != null -> TODO()
        id != null && version == null -> PropKind.Id
        id == null && version != null -> PropKind.Version
        else -> PropKind.Basic
    }
    val columnName = column?.name ?: kParameter.name!!
    return PropMeta(kParameter, kProperty, kind, columnName)
}