package ru.misterpotz.ocgena.simulation.interactors.original

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.binding.EnabledBinding
import ru.misterpotz.ocgena.simulation.binding.EnabledBindingWithTokens
import ru.misterpotz.ocgena.simulation.interactors.ArcPrePlaceHasEnoughTokensChecker
import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingResolverInteractor
import ru.misterpotz.ocgena.simulation.interactors.TokenSelectionInteractor
import ru.misterpotz.ocgena.simulation.state.CurrentSimulationDelegate
import ru.misterpotz.ocgena.simulation.state.original.CurrentSimulationStateOriginal
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import java.util.*
import javax.inject.Inject

class EnabledBindingResolverInteractorOriginalImpl @Inject constructor(
    private val tokenSelectionInteractor: TokenSelectionInteractor,
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val ocNetInstance: SimulatableOcNetInstance,
    private val currentSimulationStateOriginal: CurrentSimulationStateOriginal,
    private val arcPrePlaceHasEnoughTokensChecker: ArcPrePlaceHasEnoughTokensChecker,
) : EnabledBindingResolverInteractor, CurrentSimulationDelegate by currentSimulationDelegate {

    private fun arcInputPlaceHasEnoughTokens(place: PetriAtomId, transition: Transition): Boolean {
        val arc = place.arcIdTo(transition.id)
        val arcMultiplicity = ocNetInstance.state.arcsMultiplicityRegistry.transitionInputMultiplicity(arcId = arc)
        return arcMultiplicity.inputPlaceHasEnoughTokens()
    }

    private fun getObjectTokens(
        transition: Transition,
        place: PetriAtomId,
    ): SortedSet<ObjectTokenId> {
        val arcMultiplicity = state.arcsMultiplicityRegistry.transitionInputMultiplicity(place.arcIdTo(transition.id))
        val requiredTokensAmount = arcMultiplicity.requiredTokenAmount()
        val selectedAndInitializedTokens = tokenSelectionInteractor.selectAndInitializeTokensFromPlace(
            petriAtomId = place,
            amount = requiredTokensAmount,
        )
        pMarking[place].addAll(selectedAndInitializedTokens.initialized)
        return selectedAndInitializedTokens.selected
    }

    override fun tryGetEnabledBinding(transition: Transition): EnabledBinding? {
        val canBeEnabled = currentSimulationStateOriginal.tTimesMarking.isAllowedToBeEnabled(transition.id)
        if (!canBeEnabled) return null

        val hasEnoughTokensAtAllInputs = transitionsRegistry[transition.id].fromPlaces.all { placeId ->
            arcInputPlaceHasEnoughTokens(placeId, transition)
        }

        if (!hasEnoughTokensAtAllInputs) {
            return null
        }

        return EnabledBinding(
            transition = transition,
        )
    }

    override fun requireEnabledBindingWithTokens(objectBinding: EnabledBinding): EnabledBindingWithTokens {
        val transition = objectBinding.transition
        val inputPlaces = transition.fromPlaces

        val placeToObjectTokenMap = buildMap {
            for (inputPlace in inputPlaces) {
                put(inputPlace, getObjectTokens(transition, inputPlace))
            }
        }

        return EnabledBindingWithTokens(
            transition = transition.id, involvedObjectTokens = ImmutablePlaceToObjectMarking(placeToObjectTokenMap)
        )
    }
}