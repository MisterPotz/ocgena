package ru.misterpotz.ocgena.registries

import kotlinx.coroutines.processNextEventInCurrentThread
import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcType
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import javax.inject.Inject


class PlaceCharacteristic(
    val objectTypeId: ObjectTypeId,
    val arc: ArcType,
)

class InputPlacesData(
    val transitionId: PetriAtomId,
    val placeToCharacteristic: MutableMap<PetriAtomId, PlaceCharacteristic> = mutableMapOf()
)


// from each place at marking directly to its related buffer stack


class TokenStackFiller(
    private val transition: PetriAtomId,
    private val petriAtomRegistry: PetriAtomRegistry,
    private val pmarking: PlaceToObjectMarking
) {
    fun fillOutputMarking(transitionInstance: TransitionInstance) {
        val transition = petriAtomRegistry.getTransition(transition)
        for (i in transition.fromPlaces) {
            val
        }
    }
}

class InputPlacesIndexRegistry @Inject constructor(
    private val petriAtomRegistry: PetriAtomRegistry,
) {
    val transitionToInputPlaceData: MutableMap<PetriAtomId, InputPlacesData> = mutableMapOf()

    fun indexTransition(transition: PetriAtomId) {
        val inputPlaces: MutableList<PetriAtomId> = petriAtomRegistry.getTransition(transition).fromPlaces


        for (inputPlace in inputPlaces) {

        }
    }
}