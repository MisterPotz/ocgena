package model.typel

interface ArcExpression {
    val variable: String?
    val stringExpr : String
    fun substitute(variableValue: Int) : Int
}

