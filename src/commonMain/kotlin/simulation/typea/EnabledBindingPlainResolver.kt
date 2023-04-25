package simulation.typea

import model.Arcs
import model.ImmutableObjectMarking
import model.Place
import model.Transition
import model.aalst.ArcMultiplicityTypeA
import simulation.binding.EnabledBindingResolver
import model.ObjectMarking
import model.ObjectToken
import simulation.PMarkingProvider
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingWithTokens
import simulation.random.TokenSelector


class EnabledBindingPlainResolver(
    private val pMarkingProvider : PMarkingProvider,
    private val arcMultiplicity: ArcMultiplicityTypeA,
    val arcs: Arcs,
    private val tokenSelector: TokenSelector,
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
                put(place, getObjectTokens(transition, place).toMutableSet())
            }
        }
        return EnabledBindingWithTokens(
            transition = transition,
            ImmutableObjectMarking(placeToObjectTokenMap.toMutableMap())
        )
    }


    override fun checkBinding(objectBinding: EnabledBinding): Boolean {
        val transition = objectBinding.transition

        return transition.inputPlaces.all { place ->
            isNormalArcAndEnoughTokens(place, transition)
                    || isVariableArcAndEnoughTokens(place, transition)
        }
    }
}
