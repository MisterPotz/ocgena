package ru.misterpotz.simulation.typea

import model.*
import model.typea.VariableArcTypeA
import model.typel.VariableArcTypeL
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.transitions.TransitionInstance
import ru.misterpotz.simulation.logging.loggers.CurrentSimulationDelegate
import simulation.LockedTokensMover
import javax.inject.Inject

class OutputMarkingFillerTypeAFactory @Inject constructor(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val lockedTokensMover: LockedTokensMover
) {
    fun create(activeFiringTransition: TransitionInstance): OutputMarkingFillerTypeA {
        return OutputMarkingFillerTypeA(
            transitionInstance = activeFiringTransition,
            lockedTokensMover = lockedTokensMover,
            currentSimulationDelegate = currentSimulationDelegate,
        )
    }
}

class OutputMarkingFillerTypeA(
    private val transitionInstance: TransitionInstance,
    private val lockedTokensMover: LockedTokensMover,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
) : CurrentSimulationDelegate by currentSimulationDelegate {
    val transition = transitions.get(transitionId = transitionInstance.transition)

    private val outputPlaces = transition.outputPlaces

    fun fill(): ImmutableObjectMarking {
        return fillOutputMarking()
    }

    private fun complementUnfilledPlaces(outputMarking: ObjectMarking): ImmutableObjectMarking {
        val transitionArcs = arcs[transition]

        for (outputPlace in outputPlaces.sortedBy { it.id }) {
            val tokens = outputMarking[outputPlace.id]!!
            val arc = transitionArcs[outputPlace]
            val type = placeTyping[outputPlace]
            when (arc) {
                is NormalArc -> {
                    val need = arc.multiplicity
                    if (tokens.size < need) {
                        for (i in 0 until (tokens.size - need)) {
                            val newToken = tokenGenerationFacade.generate(type)
                            tokens.add(newToken.id)
                        }
                    }
                }

                is VariableArcTypeA -> {
                    // good as it is
                }

                is VariableArcTypeL -> {
                    // good oas it is
                }
            }
        }
        return outputMarking.toImmutable()
    }

    private fun fillOutputMarking(): ImmutableObjectMarking {
        val transitionArcs = arcs[transition]

        val outputMarking = lockedTokensMover.tryFillOutputPlacesNormalArcs(
            transitionInstance
        )

        return complementUnfilledPlaces(outputMarking)
    }
}
