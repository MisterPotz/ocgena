package ru.misterpotz

import javax.inject.Inject

class TokenSerializer @Inject constructor() {
    fun serializeTokens(tokens: List<Long>): String {
        return tokens.joinToString(separator = ",") { it.toString() }
    }

    fun deserializeTokens(string: String): List<Long> {
        return string.split(",").map { it.toLong() }
    }
}
