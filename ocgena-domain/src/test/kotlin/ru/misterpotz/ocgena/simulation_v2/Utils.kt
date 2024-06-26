package ru.misterpotz.ocgena.simulation_v2

import ru.misterpotz.Logger
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.ocnet.utils.defaultObjType
import ru.misterpotz.ocgena.ocnet.utils.defaultObjTypeId
import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_selection.ModelAccessor
import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenGenerator
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice

fun List<SimulationStepLog>.prettyString() = joinToString("\n") { it.prettyString() }

class StepSequenceLogger : Logger {
    sealed interface Event {
        data object Prepared : Event
        data class Fired(val transitionId: String) : Event {
            override fun toString(): String {
                return "Fired('$transitionId')"
            }
        }

        data object Finished : Event
    }

    val events: MutableList<Event> = mutableListOf()
    val logs: MutableList<SimulationStepLog> = mutableListOf()

    override suspend fun simulationPrepared() {
        events.add(Event.Prepared)
    }

    override suspend fun acceptStepLog(simulationStepLog: SimulationStepLog) {
        events.add(Event.Fired(simulationStepLog.selectedFiredTransition?.transitionId ?: ""))
        logs.add(simulationStepLog)
    }

    override suspend fun simulationFinished() {
        events.add(Event.Finished)
    }
}

class SizeLogger : Logger {
    sealed interface Event {
        data object Prepared : Event
        data class Fired(val transitionId: String) : Event {
            override fun toString(): String {
                return "Fired('$transitionId')"
            }
        }

        data object Finished : Event
    }

    val events: MutableList<Event> = mutableListOf()

    override suspend fun simulationPrepared() {
        events.add(Event.Prepared)
    }

    override suspend fun acceptStepLog(simulationStepLog: SimulationStepLog) {
        events.add(Event.Fired(simulationStepLog.selectedFiredTransition?.transitionId ?: ""))
    }

    override suspend fun simulationFinished() {
        events.add(Event.Finished)
    }
}

fun SimpleTokenSlice.copyFromMap(
    model: ModelAccessor,
    map: Map<String, List<Int>>
): TokenSlice {
    return SimpleTokenSlice.of(
        buildMap {
            for ((place, tokens) in map) {
                put(model.place(place), tokens.map { tokenBy(it.toLong()) })
            }
        }
    )
}

fun Map<String, List<Int>>.toTokenSliceFrom(tokenSlice: SimpleTokenSlice, model: ModelAccessor): TokenSlice {
    return tokenSlice.copyFromMap(model, this)
}

fun Map<String, Int>.toTokenSliceAmounts(model: ModelAccessor): TokenSlice {
    val map = this
    return SimpleTokenSlice.build {
        for ((place, amount) in map) {
            addAmount(model.place(place), amount)
        }
    }
}

object NoTokenGenerator : TokenGenerator {
    override fun generateRealToken(type: ObjectType): TokenWrapper {
        throw IllegalStateException("not allowed to generate")
    }

    override val issuedTokens: Map<Long, TokenWrapper> = emptyMap()
}

fun buildTransitionHistory(
    model: ModelAccessor,
    transitionToHistoryEntries: Map<String, List<List<Int>>>,
    placeToTokens: Map<String, List<Int>>,
    placeToTypes: Map<String, Int> = mapOf()
): SimpleTokenSlice {
    val tokenEntries = mutableMapOf<String, TokenWrapper>()
    val objectTypes = mutableMapOf<String, ObjectType>()
    fun List<List<Int>>.recordTokensToHistory(transitionWrapper: TransitionWrapper): List<TokenWrapper> {
        return flatMap { it ->
            val tokens = it.map { tokenIndex ->
                tokenEntries.getOrPut(tokenIndex.toString()) {
                    TokenWrapper(
                        tokenIndex.toLong(),
                        (placeToTokens.map { Pair(it.key, it.value) }
                            .find { it.second.contains(tokenIndex) }!!.first).let {
                                val type = placeToTypes[it]
                                objectTypes.computeIfAbsent(it) { ObjectType(type?.toString() ?: defaultObjTypeId) }
                            }
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
        return find { it.tokenId == id.toLong() }!!
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

fun buildTokenSlice(
    model: ModelAccessor,
    placeToTokens: Map<String, List<Int>>
): SimpleTokenSlice {

    fun List<TokenWrapper>.by(int: Int): TokenWrapper {
        val id = int.toString()
        return find { it.tokenId == id.toLong() }!!
    }

    val allTokens = placeToTokens.flatMap { (place, ints) ->
        ints.map {
            TokenWrapper(
                it.toLong(),
                model.defaultObjectType()
            )
        }

    }

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