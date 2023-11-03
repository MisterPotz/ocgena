package simulation.utils

import model.*
import ru.misterpotz.marking.objects.ObjectTokenId
import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.model.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.PlaceId
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.simulation.api.interactors.RepeatabilityInteractor
import ru.misterpotz.simulation.api.interactors.TokenSelectionInteractor
import ru.misterpotz.simulation.logging.loggers.CurrentSimulationDelegate
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
            currentSimulationDelegate = currentSimulationDelegate
        )
    }
}

class TokenInputCollectorByTypeAndArc(
    transitionInstance: TransitionInstance,
    private val repeatabilityInteractor: RepeatabilityInteractor,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    currentSimulationDelegate: CurrentSimulationDelegate,
) {
    private val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry = currentSimulationDelegate.placeTyping
    private val arcsRegistry: ArcsRegistry = currentSimulationDelegate.arcs
    private val transitionsRegistry: TransitionsRegistry = currentSimulationDelegate.transitions
    private val transitionInputMarking = transitionInstance.lockedObjectTokens
    private val transition = transitionsRegistry[transitionInstance.transition]
    private val transitionInputPlaces = transition.inputPlaces
    private val transitionArcs = arcsRegistry[transition]


    private val collectedThroughNormalArcsAcc = mutableMapOf<ObjectType, MutableList<ObjectTokenId>>()
    private val collectedThroughOtherArcsAcc = mutableMapOf<ObjectType, MutableList<ObjectTokenId>>()

    private fun addToNormalForPlaceAndType(placeId: PlaceId, type: ObjectType) {
        val placeObjectTokens = transitionInputMarking[placeId]!!
        val acc = collectedThroughNormalArcsAcc.getOrPut(type) { mutableListOf() }
        acc.addAll(placeObjectTokens)
    }

    private fun addToOtherForPlaceAndType(placeId: PlaceId, type: ObjectType) {
        val placeObjectTokens = transitionInputMarking[placeId]!!
        val acc = collectedThroughOtherArcsAcc.getOrPut(type) { mutableListOf() }
        acc.addAll(placeObjectTokens)
    }

    fun collectTokensByInputArcAndType(): ObjectTokenStacks {
        val inputPlaceObjects =
            repeatabilityInteractor
                .sortPlaces(places = transitionInputPlaces)
                .groupBy {
                    placeToObjectTypeRegistry[it.id]
                }
        inputPlaceObjects.forEach { (type, places) ->
            places.forEach { place ->
                val arcWhichReachesPlace = transitionArcs[place]
                when (arcWhichReachesPlace) {
                    is NormalArc -> addToNormalForPlaceAndType(place.id, type)
                    else -> addToOtherForPlaceAndType(place.id, type)
                }
            }
        }

        fun shuffleCollectionsAndConvertToStacks(map: Map<ObjectType, MutableList<ObjectTokenId>>): Map<ObjectType, ObjectTokenStack> {
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
        val collectedThroughNormalArcs: Map<ObjectType, ObjectTokenStack>,
        val collectedThroughOtherArcs: Map<ObjectType, ObjectTokenStack>
    )
}
