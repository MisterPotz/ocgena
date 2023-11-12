package ru.misterpotz.expression.error

import java.lang.Exception

class UnrecognizedTokenError(val unrecognizedToken: String) :
    Exception("encountered unexpected token during parsing: $unrecognizedToken")