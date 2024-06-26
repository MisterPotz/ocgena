package ru.misterpotz.ocgena.simulation_v2.entities_selection

import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_v2.entities.*
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.utils.Ref
import ru.misterpotz.ocgena.simulation_v2.utils.selectIn

class ModelAccessor(
    val ocNet: OCNetStruct,
    val simulationInput: SimulationInput
) {
    val transitionsRef: Ref<Transitions> = Ref()
    val placesRef: Ref<Places> = Ref()

    val transitionIdIssuer = TransitionIdIssuer()
    val outPlaces: Places by lazy {
        placesRef.ref.places.selectIn(ocNet.outputPlaces.iterable).wrap()
    }

    val loggedTransitions by lazy {
        transitionsRef.ref.flatMap { it.inputArcConditions.map { it.syncTarget } }.toSet().toList()
    }

    fun isSynchronizedMode(): Boolean {
        return loggedTransitions.isNotEmpty()
    }

    fun tokensAreEntities(): Boolean {
        return simulationInput.loggingEnabled ?: false
    }

    fun transitionBy(id: PetriAtomId) = transitionsRef.ref.map[id]!!
    fun place(id: PetriAtomId) = placesRef.ref.map[id]!!

    fun defaultObjectType() = ocNet.objectTypeRegistry.types.first()

    fun init() {
        transitionsRef.setRef(
            ocNet.transitionsRegistry.map {
                TransitionWrapper(
                    it.id,
                    model = this,
                )
            }.wrap()
        )

        placesRef.setRef(
            ocNet.placeRegistry.places.map {
                PlaceWrapper(
                    it.id,
                    objectType = ocNet.objectTypeRegistry.get(ocNet.placeToObjectTypeRegistry[it.id]!!)
                )
            }.wrap()
        )
    }
}