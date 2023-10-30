package simulation.typea

import model.*
import model.typea.ArcMultiplicityTypeA
import ru.misterpotz.model.marking.ImmutableObjectMarking
import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.model.marking.ObjectToken
import ru.misterpotz.simulation.marking.PMarkingProvider
import ru.misterpotz.simulation.transition.TransitionOccurrenceAllowedTimes
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingResolver
import simulation.binding.EnabledBindingWithTokens
import simulation.random.TokenSelector


class EnabledBindingTypeAResolver(
    private val pMarkingProvider: PMarkingProvider,
    private val arcMultiplicity: ArcMultiplicityTypeA,
    val arcs: Arcs,
    private val tokenSelector: TokenSelector,
    private val tTimes: TransitionOccurrenceAllowedTimes
) : EnabledBindingResolver {

    private val pMarking : ObjectMarking
        get() = pMarkingProvider.pMarking

    private fun isNormalArcAndEnoughTokens(place: Place, transition: Transition): Boolean {
        val marking = pMarking[place]
        val arc = arcs[transition][place]!!
        val requiredTokens = arcMultiplicity.getMultiplicity(arc)

        return !arcMultiplicity.isVariable(arc) && (marking?.size ?: 0) >= requiredTokens
    }

    private fun isVariableArcAndEnoughTokens(place: Place, transition: Transition): Boolean {
        val marking = pMarking[place]
        val arc = arcs[transition][place]!!
        return arcMultiplicity.isVariable(arc) && (marking?.size ?: 0) > 1
    }

    private fun getObjectTokens(
        transition: Transition,
        place: Place,
    ): Set<ObjectToken> {
        // different objects policy can be setup here
        // e.g., randomized or sorted by object token time
        val arc = arcs[transition][place]!!
        val marking = pMarking[place]!!
        val requiredNormal = arcMultiplicity.getMultiplicity(arc) ?: 1
        return if (arcMultiplicity.isVariable(arc).not()) {
            tokenSelector.getTokensFromSet(marking, requiredNormal)
        } else {
            marking
        }
    }

    override fun tryGetEnabledBinding(transition: Transition): EnabledBinding? {
        val canBeEnabled = tTimes.isAllowedToBeEnabled(transition)
        if (!canBeEnabled)
            return null

        val inputPlaces = transition.inputPlaces

        val placesWithEnoughTokens = inputPlaces.filter { place ->

            isNormalArcAndEnoughTokens(place, transition)
                    || isVariableArcAndEnoughTokens(place, transition)
        }
        if (placesWithEnoughTokens.size != inputPlaces.size) {
            return null
        }

        return EnabledBinding(
            transition = transition,
        )
    }

    override fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding): EnabledBindingWithTokens {
        val transition = objectBinding.transition
        val inputPlaces =  transition.inputPlaces

        val placesWithEnoughTokens = inputPlaces.filter { place ->
            isNormalArcAndEnoughTokens(place, transition)
                    || isVariableArcAndEnoughTokens(place, transition)
        }

        if (placesWithEnoughTokens.size != inputPlaces.size) {
            throw IllegalStateException("transition couldn't be created as not enough tokens are in the marking")
        }

        val placeToObjectTokenMap = buildMap {
            placesWithEnoughTokens.forEach { place ->
                put(place.id, getObjectTokens(transition, place).toMutableSet())
            }
        }
        return EnabledBindingWithTokens(
            transition = transition,
            ImmutableObjectMarking(placeToObjectTokenMap.toMutableMap())
        )
    }
}
