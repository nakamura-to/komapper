package org.komapper.core.builder

data class Column(val alias: String, val name: String, val value: String = "$alias.$name") : CharSequence by value {
    override fun toString(): String {
        return value
    }
}
