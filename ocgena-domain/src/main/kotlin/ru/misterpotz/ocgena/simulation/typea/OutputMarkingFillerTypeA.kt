package ru.misterpotz.ocgena.simulation.typea

import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionInstance
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcTypeA
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArcTypeL
import ru.misterpotz.ocgena.simulation.interactors.RepeatabilityInteractor
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.LockedTokensMover
import javax.inject.Inject

class OutputMarkingFillerTypeAFactory @Inject constructor(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val lockedTokensMover: LockedTokensMover,
    private val repeatabilityInteractor: RepeatabilityInteractor,
) {
    fun create(activeFiringTransition: TransitionInstance): OutputMarkingFillerTypeA {
        return OutputMarkingFillerTypeA(
            transitionInstance = activeFiringTransition,
            lockedTokensMover = lockedTokensMover,
            currentSimulationDelegate = currentSimulationDelegate,
            repeatabilityInteractor = repeatabilityInteractor
        )
    }
}

class OutputMarkingFillerTypeA(
    private val transitionInstance: TransitionInstance,
    private val lockedTokensMover: LockedTokensMover,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val repeatabilityInteractor: RepeatabilityInteractor
) : CurrentSimulationDelegate by currentSimulationDelegate {
    val transition = petriAtomRegistry.getTransition(transitionInstance.transition)

    private val outputPlaces = transition.outputPlaces

    fun fill(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        return fillOutputMarking()
    }

    private fun complementUnfilledPlaces(outputMarking: PlaceToObjectMarking): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        for (outputPlace in repeatabilityInteractor.sortPlaces(outputPlaces)) {
            val tokens = outputMarking[outputPlace]!!
            with(petriAtomRegistry) {
                val arc = transition.id.arcTo(outputPlace)
                val type = placeToObjectTypeRegistry[outputPlace]
                when (arc) {
                    is NormalArc -> {
                        val need = arc.multiplicity
                        if (tokens.size < need) {
                            for (i in 0 until (tokens.size - need)) {
                                val newToken = newTokenTimeBasedGenerationFacade.generate(type)
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
        }
        return outputMarking.toImmutable()
    }

    private fun fillOutputMarking(): ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking {
        val outputMarking = lockedTokensMover.tryFillOutputPlacesNormalArcs(
            transitionInstance
        )

        return complementUnfilledPlaces(outputMarking)
    }
}
