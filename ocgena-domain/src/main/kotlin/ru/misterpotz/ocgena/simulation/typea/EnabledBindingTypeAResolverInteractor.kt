package simulation.typea

import model.*
import ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityTypeARegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingResolverInteractor
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import simulation.binding.EnabledBinding
import simulation.binding.EnabledBindingWithTokens
import java.util.*
import javax.inject.Inject


class EnabledBindingTypeAResolverInteractor @Inject constructor(
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val currentSimulationDelegate: CurrentSimulationDelegate
) : EnabledBindingResolverInteractor, CurrentSimulationDelegate by currentSimulationDelegate {

    private val arcMultiplicity get() = ocNet.arcMultiplicity as ArcToMultiplicityTypeARegistry

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
    ): SortedSet<ObjectTokenId> {
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
        val canBeEnabled = tTimesMarking.isAllowedToBeEnabled(transition.id)
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

        val inputPlacesWithEnoughTokens = inputPlaces.filter { place ->
            isNormalArcAndEnoughTokens(place, transition)
                    || isVariableArcAndEnoughTokens(place, transition)
        }

        if (inputPlacesWithEnoughTokens.size != inputPlaces.size) {
            throw IllegalStateException("transition couldn't be created as not enough tokens are in the marking")
        }

        val placeToObjectTokenMap = buildMap {
            inputPlacesWithEnoughTokens.forEach { place ->
                put(place.id, getObjectTokens(transition, place))
            }
        }
        return EnabledBindingWithTokens(
            transition = transition.id,
            involvedObjectTokens = ImmutablePlaceToObjectMarking(placeToObjectTokenMap)
        )
    }
}
