package ru.misterpotz.expression

import java.lang.IllegalStateException

object StringTokenizer {

    fun tokenize(expression: String): List<String> {
        var i = 0
        val cleanedExpression = expression.replace("\\s".toRegex(), "")

        val builtTokensList = mutableListOf<String>()

        while (i < cleanedExpression.length) {
            val matchResult = tokenMatcherRegex.find(cleanedExpression, startIndex = i)
            if (matchResult?.value != null) {
                builtTokensList.add(matchResult.value)
            } else {
                throw IllegalStateException("tokenization failed")
            }
            i += matchResult.value.length
        }

        return builtTokensList
    }
}
