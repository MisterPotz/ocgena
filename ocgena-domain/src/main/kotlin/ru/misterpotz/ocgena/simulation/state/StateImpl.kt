package ru.misterpotz.ocgena.simulation.state

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.structure.State
import javax.inject.Inject

class PMarkingProvider @Inject constructor(private val ocNet: OCNet) {
    private val placeToObjectMarking by lazy(LazyThreadSafetyMode.NONE) {
        PlaceToObjectMarking()
    }
    fun get() : PlaceToObjectMarking {
        return placeToObjectMarking
    }
}

class StateImpl @Inject constructor(
    ocNet: OCNet,
    override val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    val pMarkingProvider: PMarkingProvider,
) : State {
    override val tMarking: TransitionToInstancesRegistry = TransitionToInstancesRegistry()
    override val pMarking: PlaceToObjectMarking = pMarkingProvider.get()
    override val tTimesMarking: TransitionToTimeUntilInstanceAllowedRegistry =
        TransitionToTimeUntilInstanceAllowedMarking()
    override val transitionToPlacesByObjectTypeIndexRegistry: TransitionToPlacesByObjectTypeIndexRegistry =
        TransitionToPlacesByObjectTypeIndexRegistry(
            transitionsRegistry = ocNet.transitionsRegistry,
            placeRegistry = ocNet.placeRegistry,
            placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry,
        )
}
