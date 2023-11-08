package ru.misterpotz.expression.paramspace

class VariableParameterSpace(vararg pairs: Pair<String, Double>) : ParameterSpace {
    val map = mapOf(*pairs)
    override fun get(parameterName: String): Double {
        return map[parameterName]!!
    }
}