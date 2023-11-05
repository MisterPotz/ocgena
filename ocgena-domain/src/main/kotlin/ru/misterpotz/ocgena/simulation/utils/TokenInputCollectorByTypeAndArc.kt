package ru.misterpotz.ocgena.simulation.utils

import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.interactors.RepeatabilityInteractor
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import javax.inject.Inject

class TokenCollectorByTypeAndArcFactory @Inject constructor(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val repeatabilityInteractor: RepeatabilityInteractor,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
) : CurrentSimulationDelegate by currentSimulationDelegate {
    fun create(transitionInstance: TransitionInstance): TokenInputCollectorByTypeAndArc {
        return TokenInputCollectorByTypeAndArc(
            transitionInstance = transitionInstance,
            repeatabilityInteractor = repeatabilityInteractor,
            tokenSelectionInteractor = tokenSelectionInteractor,
            currentSimulationDelegate = currentSimulationDelegate,
        )
    }
}

class TokenInputCollectorByTypeAndArc(
    transitionInstance: TransitionInstance,
    private val repeatabilityInteractor: RepeatabilityInteractor,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    currentSimulationDelegate: CurrentSimulationDelegate,
) {
    val ocNet = currentSimulationDelegate.ocNet.ocNet
    private val transitionInputMarking = transitionInstance.lockedObjectTokens
    private val transition = ocNet.transitionsRegistry[transitionInstance.transition]
    private val transitionInputPlaces = transition.fromPlaces
    private val placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry
    private val collectedThroughNormalArcsAcc = mutableMapOf<ObjectTypeId, MutableList<ObjectTokenId>>()
    private val collectedThroughOtherArcsAcc = mutableMapOf<ObjectTypeId, MutableList<ObjectTokenId>>()
    private val arcsRegistry = ocNet.arcsRegistry
    private fun addToNormalForPlaceAndType(placeId: PetriAtomId, type: ObjectTypeId) {
        val placeObjectTokens = transitionInputMarking[placeId]!!
        val acc = collectedThroughNormalArcsAcc.getOrPut(type) { mutableListOf() }
        acc.addAll(placeObjectTokens)
    }

    private fun addToOtherForPlaceAndType(placeId: PetriAtomId, type: ObjectTypeId) {
        val placeObjectTokens = transitionInputMarking[placeId]!!

        val acc = collectedThroughOtherArcsAcc.getOrPut(type) { mutableListOf() }
        acc.addAll(placeObjectTokens)
    }

    fun collectTokensByInputArcAndType(): ObjectTokenStacks {
        val inputPlaceObjects =
            repeatabilityInteractor
                .sortPlaces(places = transitionInputPlaces)
                .groupBy {
                    placeToObjectTypeRegistry[it]
                }
        inputPlaceObjects.forEach { (type, places) ->
            places.forEach { place ->
                val arcId = place.arcIdTo(transition.id)
                val arc = arcsRegistry[arcId]
                when (arc) {
                    is NormalArc -> addToNormalForPlaceAndType(place, type)
                    else -> addToOtherForPlaceAndType(place, type)
                }
            }
        }

        fun shuffleCollectionsAndConvertToStacks(map: Map<ObjectTypeId, MutableList<ObjectTokenId>>): Map<ObjectTypeId, ObjectTokenStack> {
            return map.mapValues { (_, tokens) ->
                ObjectTokenStack(tokenSelectionInteractor.shuffleTokens(tokens))
            }
        }

        return ObjectTokenStacks(
            collectedThroughNormalArcs = shuffleCollectionsAndConvertToStacks(collectedThroughNormalArcsAcc),
            collectedThroughOtherArcs = shuffleCollectionsAndConvertToStacks(collectedThroughOtherArcsAcc)
        )
    }

    class ObjectTokenStacks(
        val collectedThroughNormalArcs: Map<ObjectTypeId, ObjectTokenStack>,
        val collectedThroughOtherArcs: Map<ObjectTypeId, ObjectTokenStack>
    )
}
