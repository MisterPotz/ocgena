package model.typel

interface ArcExpression {
    val variable: String
    fun substitute(variableValue: Int) : Int
}

