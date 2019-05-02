package koma.sql

class SqlLocation(private val sql: String, val lineNumber: Int, val position: Int) {
    override fun toString(): String {
        return "<$sql>:$lineNumber:$position"
    }
}
