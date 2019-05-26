package koma

@Target(AnnotationTarget.CLASS)
annotation class Table(val name: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Id

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SequenceGenerator(val name: String, val incrementBy: Int)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Version

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Column(val name: String)
