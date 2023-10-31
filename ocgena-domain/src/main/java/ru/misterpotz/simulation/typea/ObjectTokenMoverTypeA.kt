package simulation.typea

import model.*
import model.typea.VariableArcTypeA
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking
import simulation.ObjectTokenMover
import simulation.random.TokenSelector
import simulation.utils.TokenCollectorByTypeAndArc

class ObjectTokenMoverTypeA(
    private val tokenSelector: TokenSelector
) : ObjectTokenMover() {
    // same type, same arc variability, arcs normal
    override fun tryFillOutputPlacesNormalArcs(
        transitionArcs: Arcs.WithTransitionGetter,
        placeTyping: PlaceTyping,
        inputPlaces: List<Place>,
        outputPlaces: List<Place>,
        inputMarking: ImmutableObjectMarking
    ): ObjectMarking {
        val tokenCollectorByTypeAndArc = TokenCollectorByTypeAndArc(
            inputMarking,
            placeTyping,
            inputPlaces,
            transitionArcs,
            tokenSelector
        )
        val normalTokens = tokenCollectorByTypeAndArc.getTokensThroughNormalArcs()
        val variableTokens = tokenCollectorByTypeAndArc.getTokensThroughVariableArcs()

        val outputMarking = ObjectMarking()

        for (outputPlace in outputPlaces.sortedBy { it.id }) {
            val arc = transitionArcs[outputPlace]
            val type = placeTyping[outputPlace]

            val consumedTokens  = when (arc) {
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
            outputMarking[outputPlace] = consumedTokens?.toSet() ?: setOf()
        }
        return outputMarking
    }
}
