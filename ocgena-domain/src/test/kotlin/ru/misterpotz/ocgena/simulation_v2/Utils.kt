package ru.misterpotz.ocgena.simulation_v2

import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice

fun SimpleTokenSlice.copyFromMap(
    model: ModelAccessor,
    map: Map<String, List<Int>>
): TokenSlice {
    return SimpleTokenSlice.of(
        buildMap {
            for ((place, tokens) in map) {
                put(model.place(place), tokens.map { tokenBy(it.toString()) })
            }
        }
    )
}

fun Map<String, List<Int>>.toTokenSliceFrom(tokenSlice: SimpleTokenSlice, model: ModelAccessor): TokenSlice {
    return tokenSlice.copyFromMap(model, this)
}


fun buildTransitionHistory(
    model: ModelAccessor,
    transitionToHistoryEntries: Map<String, List<List<Int>>>,
    placeToTokens: Map<String, List<Int>>

): SimpleTokenSlice {
    val tokenEntries = mutableMapOf<String, TokenWrapper>()
    fun List<List<Int>>.recordTokensToHistory(transitionWrapper: TransitionWrapper): List<TokenWrapper> {
        return flatMap { it ->
            val tokens = it.map { tokenIndex ->
                tokenEntries.getOrPut(tokenIndex.toString()) {
                    TokenWrapper(
                        tokenIndex.toString(),
                        model.defaultObjectType()
                    )
                }
            }
            val newLogReference = transitionWrapper.getNewTransitionReference()
            for (token in tokens) {
                transitionWrapper.addTokenVisit(newLogReference, token)
            }
            tokens
        }
    }

    fun List<TokenWrapper>.by(int: Int): TokenWrapper {
        val id = int.toString()
        return find { it.tokenId == id }!!
    }

    val allTokens = transitionToHistoryEntries.map { it.value to model.transitionBy(it.key) }
        .flatMap { (history, transition) -> history.recordTokensToHistory(transition) }

    fun List<Int>.selectTokens(): List<TokenWrapper> {
        return map { allTokens.by(it) }
    }

    val tokenSlice = SimpleTokenSlice.build {
        for ((place, tokens) in placeToTokens) {
            addTokens(model.place(place), tokens.selectTokens())
        }
    }

    return tokenSlice
}