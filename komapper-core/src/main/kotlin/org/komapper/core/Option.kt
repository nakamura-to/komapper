package org.komapper.core

import kotlin.reflect.KProperty1

/**
 * A option for a insert command.
 */
data class InsertOption(
    val assignId: Boolean = true,
    val assignTimestamp: Boolean = true,
    val include: List<KProperty1<*, *>> = emptyList(),
    val exclude: List<KProperty1<*, *>> = emptyList()
)

/**
 * A option for a delete command.
 */
data class DeleteOption(
    val ignoreVersion: Boolean = false
)

/**
 * A option for a update command.
 */
data class UpdateOption(
    val incrementVersion: Boolean = true,
    val updateTimestamp: Boolean = true,
    val ignoreVersion: Boolean = false,
    val include: List<KProperty1<*, *>> = emptyList(),
    val exclude: List<KProperty1<*, *>> = emptyList()
)
