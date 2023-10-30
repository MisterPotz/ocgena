package simulation.utils

import model.*
import ru.misterpotz.model.marking.ImmutableObjectMarking
import simulation.random.TokenSelector

class TokenCollectorByTypeAndArc(
    private val inputMarking: ImmutableObjectMarking,
    val placeTyping: PlaceTyping,
    val inputPlaces: List<Place>,
    private val transitionArcs: Arcs.WithTransitionGetter,
    private val tokenSelector: TokenSelector
) {
    private fun getTokensFromPlaces(typeToPlaces: Map<ObjectType, List<Place>>) : Map<ObjectType, ObjectTokenStack> {
        return buildMap {
            for ((type, places) in typeToPlaces) {
                val allObjectTokens = places.flatMap { inputMarking[it]!! }
                val shuffledTokens = tokenSelector.shuffleTokens(allObjectTokens)

                put(type, ObjectTokenStack(shuffledTokens))
            }
        }
    }

    fun getTokensThroughNormalArcs() : Map<ObjectType, ObjectTokenStack> {
        val normalPlaces = inputPlaces.filter { transitionArcs[it] is NormalArc }
        val typeToPlaces = normalPlaces.groupBy { placeTyping[it] }

        return getTokensFromPlaces(typeToPlaces)
    }

    fun getTokensThroughVariableArcs() : Map<ObjectType, ObjectTokenStack> {
        val variablePlaces = inputPlaces.filter { transitionArcs[it] !is NormalArc }
        val typeToPlaces = variablePlaces.groupBy { placeTyping[it] }
        return getTokensFromPlaces(typeToPlaces)
    }
}
