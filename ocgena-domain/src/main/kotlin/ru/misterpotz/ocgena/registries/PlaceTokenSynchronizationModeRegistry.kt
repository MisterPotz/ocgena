package ru.misterpotz.ocgena.registries

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId

class TransitionSynchronizationRegistry(
    private val transitionToInPlaces: MutableMap<PetriAtomId, InPlacesTransitionTokenSynchronization>
) {
    fun getTokenSynchronizationMode(place: PetriAtomId): InPlacesTransitionTokenSynchronization {
        return transitionToInPlaces[place]!!
    }
}

class InPlacesTransitionTokenSynchronization(
    val inPlacesToSynchronizationTargets: Map<PetriAtomId, SynchronizationTarget>
)

class OutPlacesTransitionTokenAssociation(
    val outPlacesToAssociationTargets: Map<PetriAtomId, SynchronizationTarget>
)

data class SynchronizationTarget(
    val petriAtomId: PetriAtomId,
    val mode: TokenSynchronizationMode
)

enum class TokenSynchronizationMode(val message: String) {
    FREE_PASS("All tokens flow in with no synchronization required and no association happens"),

    SYNCHRONIZE_REQUIRED("All tokens flow in with synchronization required"),

    ASSOCIATE("Tokens flow in without synchronization required and are associated on out")
}
