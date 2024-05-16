package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ObjectTokenMeta
import ru.misterpotz.SimulationLogTransition
import ru.misterpotz.SimulationStepLog
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import kotlin.properties.Delegates

class LogBuilder {
    private var stepNumber: Long by Delegates.notNull()
    private var clockIncrement: Long by Delegates.notNull()

    private var selectedFiredTransition: SimulationLogTransition? = null

    private var starterMarkingAmounts: Map<PetriAtomId, Int> by Delegates.notNull()

    private var stepEndMarking: Map<PetriAtomId, Int>? = null

    private var firingInMarkingAmounts: Map<PetriAtomId, Int> by Delegates.notNull()
    private var firingInMarkingTokens: Map<PetriAtomId, List<Long>> by Delegates.notNull()
    private var firingOutMarkingAmounts: Map<PetriAtomId, Int> by Delegates.notNull()
    private var firingOutMarkingTokens: Map<PetriAtomId, List<Long>> by Delegates.notNull()

    private var tokensInitializedAtStep: List<ObjectTokenMeta> by Delegates.notNull()

    private fun TokenSlice.toAmountsMap(): Map<PetriAtomId, Int> {
        val slice = this
        return buildMap {
            for (place in slice.relatedPlaces) {
                put(place.placeId, slice.amountAt(place))
            }
        }
    }

    private fun TokenSlice.toMaps(): Pair<Map<PetriAtomId, Int>, Map<PetriAtomId, List<Long>>> {
        val slice = this
        return Pair(
            buildMap {
                for (place in slice.relatedPlaces) {
                    put(place.placeId, slice.amountAt(place))
                }
            },
            buildMap {
                for ((place, tokens) in byPlaceIterator()) {
                    put(place.placeId, tokens.map { it.tokenId })
                }
            }
        )
    }


    fun recordTokens(
        consumedTokens: TokenSlice,
        producedTokens: TokenSlice,
        createdTokens: List<TokenWrapper>
    ) {
        val (firingInAmounts, firingInMarkingTokens) = consumedTokens.toMaps()
        this.firingInMarkingAmounts = firingInAmounts
        this.firingInMarkingTokens = firingInMarkingTokens
        val (outAmounts, outTokens) = producedTokens.toMaps()
        this.firingOutMarkingAmounts = outAmounts
        this.firingOutMarkingTokens = outTokens

        this.tokensInitializedAtStep = createdTokens.map { ObjectTokenMeta(it.tokenId, it.objectType.id) }
    }

    fun recordStartMarking(tokenSlice: TokenSlice) {
        starterMarkingAmounts = tokenSlice.toAmountsMap()
    }

    fun recordMarking(tokenSlice: TokenSlice) {
        stepEndMarking = tokenSlice.toAmountsMap()
    }

    fun recordClockIncrement(clockIncr: Long) {
        this.clockIncrement = clockIncr
    }

    fun recordStepNumber(stepNumber: Long) {
        this.stepNumber = stepNumber
    }

    fun recordFiredTransition(transitionWrapper: TransitionWrapper) {
        selectedFiredTransition = SimulationLogTransition(
            transitionWrapper.transitionId,
            transitionDuration = transitionWrapper.timer.counter
        )
    }

    private var alreadyBuilt = false
    fun build(): SimulationStepLog {
        require(!alreadyBuilt)

        return SimulationStepLog(
            stepNumber = stepNumber,
            clockIncrement = clockIncrement,
            selectedFiredTransition = selectedFiredTransition,
            starterMarkingAmounts = starterMarkingAmounts,
            endStepMarkingAmounts = stepEndMarking,
            firingInMarkingAmounts = firingInMarkingAmounts,
            firingOutMarkingAmounts = firingOutMarkingAmounts,
            firingInMarkingTokens = firingInMarkingTokens,
            firingOutMarkingTokens = firingOutMarkingTokens,
            tokensInitializedAtStep = tokensInitializedAtStep,
        ).also { alreadyBuilt = true }
    }
}
