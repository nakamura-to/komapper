package koma.expr

class ExprLocation(val expression: String, val position: Int) {
    override fun toString(): String {
        return "<$expression>:$position"
    }
}
