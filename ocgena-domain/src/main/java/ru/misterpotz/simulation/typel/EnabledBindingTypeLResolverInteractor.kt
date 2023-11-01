package simulation.typel

import model.*
import model.typea.ArcMultiplicityTypeA
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.objects.ObjectTokenId
import ru.misterpotz.marking.plain.PMarkingProvider
import ru.misterpotz.marking.transitions.TransitionTimesMarking
import ru.misterpotz.model.atoms.Place
import ru.misterpotz.model.atoms.Transition
import simulation.binding.EnabledBinding
import ru.misterpotz.simulation.api.interactors.EnabledBindingResolverInteractor
import simulation.binding.EnabledBindingWithTokens
import ru.misterpotz.simulation.api.interactors.TokenSelectionInteractor

class EnabledBindingTypeLResolverInteractor(
    private val pMarkingProvider: PMarkingProvider,
    private val arcMultiplicity: ArcMultiplicityTypeA,
    val arcs: Arcs,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val tTimes: TransitionTimesMarking
) : EnabledBindingResolverInteractor {
    private val pMarking: ObjectMarking
        get() = pMarkingProvider.pMarking

    private fun isNormalArcAndEnoughTokens(place: Place, transition: Transition): Boolean {
        val marking = pMarking[place.id]
        val arc = arcs[transition][place]!!
        val requiredTokens = arcMultiplicity.getMultiplicity(arc)

        return !arcMultiplicity.isVariable(arc) && (marking?.size ?: 0) >= requiredTokens
    }

    private fun isVariableArcAndEnoughTokens(place: Place, transition: Transition): Boolean {
        val marking = pMarking[place.id]
        val arc = arcs[transition][place]!!
        return arcMultiplicity.isVariable(arc) && (marking?.size ?: 0) > 1
    }

    private fun getObjectTokens(
        transition: Transition,
        place: Place,
    ): Set<ObjectTokenId> {
        // different objects policy can be setup here
        // e.g., randomized or sorted by object token time
        val arc = arcs[transition][place]!!
        val marking = pMarking[place.id]!!
        val requiredNormal = arcMultiplicity.getMultiplicity(arc)
        return if (arcMultiplicity.isVariable(arc).not()) {
            tokenSelectionInteractor.getTokensFromSet(marking, requiredNormal)
        } else {
            marking
        }
    }

    override fun tryGetEnabledBinding(transition: Transition): EnabledBinding? {
        val canBeEnabled = tTimes.isAllowedToBeEnabled(transition.id)
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
        val inputPlaces = transition.inputPlaces

        val placesWithEnoughTokens = inputPlaces.filter { place ->
            isNormalArcAndEnoughTokens(place, transition)
                    || isVariableArcAndEnoughTokens(place, transition)
        }

        if (placesWithEnoughTokens.size != inputPlaces.size) {
            throw IllegalStateException("transition couldn't be created as not enough tokens are in the marking")
        }

        val placeToObjectTokenMap = buildMap {
            placesWithEnoughTokens.forEach { place ->
                put(place.id, getObjectTokens(transition, place).toSortedSet())
            }
        }
        return EnabledBindingWithTokens(
            transition = transition.id,
            involvedObjectTokens = ImmutableObjectMarking(placeToObjectTokenMap)
        )
    }
}
