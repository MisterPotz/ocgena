package simulation.typea

import model.*
import ru.misterpotz.model.arcs.VariableArcTypeA
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.model.arcs.NormalArc
import ru.misterpotz.simulation.api.interactors.RepeatabilityInteractor
import ru.misterpotz.simulation.logging.loggers.CurrentSimulationDelegate
import simulation.LockedTokensMover
import simulation.utils.TokenCollectorByTypeAndArcFactory
import javax.inject.Inject

class LockedTokensMoverTypeA @Inject constructor(
    private val repeatabilityInteractor: RepeatabilityInteractor,
    private val tokenCollectorByTypeAndArcFactory: TokenCollectorByTypeAndArcFactory,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
) : LockedTokensMover(), CurrentSimulationDelegate by currentSimulationDelegate {
    // same type, same arc variability, arcs normal
    override fun tryFillOutputPlacesNormalArcs(transitionInstance: TransitionInstance): ObjectMarking {
        val tokenInputCollectorByTypeAndArc = tokenCollectorByTypeAndArcFactory.create(transitionInstance)
        val collectedTokens = tokenInputCollectorByTypeAndArc.collectTokensByInputArcAndType()
        val normalTokens = collectedTokens.collectedThroughNormalArcs
        val variableTokens = collectedTokens.collectedThroughOtherArcs

        val outputMarking = ObjectMarking()

        val outputPlaces = transitionInstance.getOutputPlaces(transitions)
        val transitionArcs = transitionInstance.transitionArcs(arcs, transitions)

        for (outputPlace in repeatabilityInteractor.sortPlaces(outputPlaces)) {
            val arc = transitionArcs[outputPlace]
            val type = placeTyping[outputPlace]

            val consumedTokens = when (arc) {
                is NormalArc -> {
                    val tokenStack = normalTokens[type]
                    tokenStack?.tryConsume(arc.multiplicity)
                }

                is VariableArcTypeA -> {
                    val tokenStack = variableTokens[type]
                    tokenStack?.tryConsumeAll()
                }

                else -> throw IllegalStateException("not supportah this ark typah: $arc")
            }
            outputMarking[outputPlace.id] = consumedTokens?.toSortedSet() ?: sortedSetOf()
        }
        return outputMarking
    }
}
