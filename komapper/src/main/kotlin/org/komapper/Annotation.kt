package org.komapper

@Target(AnnotationTarget.CLASS)
annotation class Table(val name: String, val quote: Boolean = false)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Id

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SequenceGenerator(val name: String, val incrementBy: Int, val quote: Boolean = false)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Version

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Column(val name: String, val quote: Boolean = false)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class CreatedAt

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UpdatedAt

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Embedded
