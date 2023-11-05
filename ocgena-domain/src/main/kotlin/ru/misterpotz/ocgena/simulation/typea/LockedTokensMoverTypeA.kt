package ru.misterpotz.ocgena.simulation.typea

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.simulation.interactors.RepeatabilityInteractor
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.LockedTokensMover
import ru.misterpotz.ocgena.simulation.utils.TokenCollectorByTypeAndArcFactory
import javax.inject.Inject

class LockedTokensMoverTypeA @Inject constructor(
    private val repeatabilityInteractor: RepeatabilityInteractor,
    private val tokenCollectorByTypeAndArcFactory: TokenCollectorByTypeAndArcFactory,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
) : LockedTokensMover(), CurrentSimulationDelegate by currentSimulationDelegate {
    // same type, same arc variability, arcs normal
    override fun tryFillOutputPlacesNormalArcs(transitionInstance: TransitionInstance): PlaceToObjectMarking {
        val tokenInputCollectorByTypeAndArc = tokenCollectorByTypeAndArcFactory.create(transitionInstance)
        val collectedTokens = tokenInputCollectorByTypeAndArc.collectTokensByInputArcAndType()
        val normalTokens = collectedTokens.collectedThroughNormalArcs
        val variableTokens = collectedTokens.collectedThroughOtherArcs

        val outputMarking = PlaceToObjectMarking()

        return with(petriAtomRegistry) {
            val outputPlaces = getTransition(transitionInstance.transition).outputPlaces

            for (outputPlace in repeatabilityInteractor.sortPlaces(outputPlaces)) {
                val arc = transitionInstance.transition.arcTo(outputPlace)
                val type = placeToObjectTypeRegistry[outputPlace]

                val consumedTokens = when (arc) {
                    is NormalArc -> {
                        val tokenStack = normalTokens[type]
                        tokenStack?.tryConsume(arc.multiplicity)
                    }

                    is VariableArc -> {
                        val tokenStack = variableTokens[type]
                        tokenStack?.tryConsumeAll()
                    }

                    else -> throw IllegalStateException("not supportah this ark typah: $arc")
                }
                outputMarking[outputPlace] = consumedTokens?.toSortedSet() ?: sortedSetOf()
            }
            return outputMarking

        }
    }
}
