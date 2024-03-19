package ru.misterpotz.expression

typealias TwoSideFun = (Double, Double) -> Double
typealias UniSideFun = (Double) -> Double

const val unminus = "unminus"
const val plus = "+"
const val product = "*"
const val minus = "-"
val unaryOp = mutableSetOf("unminus")
val tokenOps = mutableSetOf("+", "-", "*")
val tokenMatcherRegex = "([0-9]+(\\.[0-9]+)?|[-+*()]|[a-zA-Z]+)".toRegex()
val processedOpSet = mutableSetOf<String>().apply {
    addAll(tokenOps)
    addAll(unaryOp)
}
val closeBrack = ")"
val openBrack = "("
val openBrackReg = Regex("""\(""")
val brackReg = Regex("""\([^()]*\)""")
val brack = mutableSetOf("(", ")")
val twoSideOp = mutableSetOf("+", "-", "*")
val variableMatcher = Regex("""[a-zA-Z]+[\da-zA-Z]*""")
val processedVariableMatcher = Regex("""var_[a-zA-Z]+[\da-zA-Z]*""")
val opPriority = mutableMapOf(
    1 to setOf(plus, minus),
    10 to setOf(product),
    100 to setOf(unminus)
)
val opToFun = mutableMapOf(
    unminus to MathsFuns.uniMinusFun,
    plus to MathsFuns.plusFun,
    minus to MathsFuns.minusFun,
    product to MathsFuns.productFun
)