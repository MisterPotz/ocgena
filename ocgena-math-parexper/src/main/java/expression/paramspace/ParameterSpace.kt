package ru.misterpotz.expression.paramspace

interface ParameterSpace {
    operator fun get(parameterName : String) : Double
}