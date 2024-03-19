package ru.misterpotz.expression.paramspace

object EmptyParameterSpace : ParameterSpace {
    override fun get(parameterName: String): Double {
        throw NotImplementedError()
    }
}