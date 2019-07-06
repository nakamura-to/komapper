package org.komapper.core

import kotlin.reflect.KProperty1

data class InsertOption(
    val assignId: Boolean = true,
    val assignTimestamp: Boolean = true,
    val include: List<KProperty1<*, *>> = emptyList(),
    val exclude: List<KProperty1<*, *>> = emptyList()
)

data class DeleteOption(
    val ignoreVersion: Boolean = false
)

data class UpdateOption(
    val incrementVersion: Boolean = true,
    val updateTimestamp: Boolean = true,
    val ignoreVersion: Boolean = false,
    val include: List<KProperty1<*, *>> = emptyList(),
    val exclude: List<KProperty1<*, *>> = emptyList()
)
