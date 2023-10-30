package simulation

import model.*
import model.typea.VariableArcTypeA
import model.typel.VariableArcTypeL
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.ObjectMarking


class OutputMarkingFiller(
    private val activeFiringTransition: ActiveFiringTransition,
    private val arcs: Arcs,
    private val placeTyping: PlaceTyping,
    private val objectTokenGenerator: ObjectTokenGenerator,
    private val objectTokenMoverFactory: ObjectTokenMoverFactory,
) {
    val transition = activeFiringTransition.transition
    private val inputPlaces = transition.inputPlaces
    private val outputPlaces = transition.outputPlaces
    private val inputMarking = activeFiringTransition.lockedObjectTokens

    private val objectTokenMover = objectTokenMoverFactory.create()

    fun fill(): ImmutableObjectMarking {
        return fillOutputMarking()
    }

    private fun complementUnfilledPlaces(outputMarking: ObjectMarking) : ImmutableObjectMarking {
        val transitionArcs = arcs[transition]

        for (outputPlace in outputPlaces.sortedBy { it.id }) {
            val tokens = outputMarking[outputPlace]!!
            val arc = transitionArcs[outputPlace]
            val type = placeTyping[outputPlace]
            when (arc ) {
                is NormalArc -> {
                    val need = arc.multiplicity
                    if (tokens.size < need) {
                        for (i in 0 until (tokens.size - need)) {
                            val newToken = objectTokenGenerator.generate(type)
                            tokens.add(newToken)
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
        return outputMarking.toImmutableMarking()
    }

    private fun fillOutputMarking(): ImmutableObjectMarking {
        val transitionArcs = arcs[transition]

        val outputMarking = objectTokenMover.tryFillOutputPlacesNormalArcs(
            transitionArcs,
            placeTyping,
            inputPlaces,
            outputPlaces,
            inputMarking
        )

        return complementUnfilledPlaces(outputMarking)
    }
}
