package org.komapper.core

/**
 * Indicates a database table.
 *
 * @property name the table name. This value can contain dots(.). For example, "schema.catalog.table".
 * @property quote whether the name is quoted or not
 */
@Target(AnnotationTarget.CLASS)
annotation class Table(val name: String, val quote: Boolean = false)

/**
 * Indicates an id property.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Id

/**
 * Indicates a sequence generator.
 *
 * This annotation must be used in conjunction with [Id].
 *
 * @property name the sequence name. This value can contain dots(.). For example, "schema.catalog.sequence".
 * @property incrementBy the value added to the current sequence value to create a new value
 * @property quote whether the name is quoted or not
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SequenceGenerator(val name: String, val incrementBy: Int, val quote: Boolean = false)

/**
 * Indicates a version property.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Version

/**
 * Indicates a database column.
 *
 * @property name the column name. This name can contain dots(.). For example, "schema.catalog.column".
 * @property quote whether the name is quoted or not
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Column(val name: String, val quote: Boolean = false)

/**
 * Indicates a timestamp property whose value is assigned just before inserting.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class CreatedAt

/**
 * Indicates a timestamp property whose value is assigned just before updating.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UpdatedAt

/**
 * Indicates an embedded property.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Embedded
