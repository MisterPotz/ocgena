package model.lomazova

interface ArcExpression {
    val variable: String
    fun substitute(variableValue: Int) : Int
}
