package simulation.typel

import model.*
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.registries.TransitionToTimeUntilInstanceAllowedRegistry
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ArcsRegistry
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.registries.typea.ArcToMultiplicityNormalDelegateTypeA
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingResolverInteractor
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.logging.loggers.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens

@Suppress("UNUSED")
class EnabledBindingTypeLResolverInteractor(
    private val arcMultiplicity: ArcToMultiplicityNormalDelegateTypeA,
    val arcsRegistry: ArcsRegistry,
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val tTimes: TransitionToTimeUntilInstanceAllowedRegistry,
    private val currentSimulationDelegate: CurrentSimulationDelegate
) : EnabledBindingResolverInteractor {
    private val pMarking: PlaceToObjectMarking get() = currentSimulationDelegate.pMarking
    private val atomRegistry : PetriAtomRegistry get() = currentSimulationDelegate.petriAtomRegistry

    private fun isNormalArcAndEnoughTokens(place: PetriAtomId, transition: Transition): Boolean {
        val marking = pMarking[place]
        val arc = arcsRegistry[transition.id.arcIdTo(place)]
        val requiredTokens = arcMultiplicity.getMultiplicity(arc)

        return !arcMultiplicity.isVariable(arc) && (marking?.size ?: 0) >= requiredTokens
    }

    private fun isVariableArcAndEnoughTokens(place: PetriAtomId, transition: Transition): Boolean {
        val marking = pMarking[place]
        val arc = arcsRegistry[transition.id.arcIdTo(place)]
        return arcMultiplicity.isVariable(arc) && (marking?.size ?: 0) > 1
    }

    private fun getObjectTokens(
        transition: Transition,
        placeId : PetriAtomId,
    ): Set<ObjectTokenId> {
        // different objects policy can be setup here
        // e.g., randomized or sorted by object token time
        val arc = arcsRegistry[transition.id.arcIdTo(placeId)]
        val marking = pMarking[placeId]!!
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
            isNormalArcAndEnoughTokens(place =  place, transition)
                    || isVariableArcAndEnoughTokens(place, transition)
        }

        if (placesWithEnoughTokens.size != inputPlaces.size) {
            throw IllegalStateException("transition couldn't be created as not enough tokens are in the marking")
        }

        val placeToObjectTokenMap = buildMap {
            placesWithEnoughTokens.forEach { place ->
                put(place, getObjectTokens(transition, place).toSortedSet())
            }
        }
        return EnabledBindingWithTokens(
            transition = transition.id,
            involvedObjectTokens = ImmutablePlaceToObjectMarking(
                placeToObjectTokenMap
            )
        )
    }
}
