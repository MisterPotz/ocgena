package ru.misterpotz.ocgena.simulation.state

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.structure.State
import javax.inject.Inject

class StateImpl @Inject constructor(
    ocNet: OCNet,
    override val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
) : State {
    override val tMarking: TransitionToInstancesRegistry = TransitionToInstancesRegistry()
    override val pMarking: PlaceToObjectMarking = PlaceToObjectMarking()
    override val tTimesMarking: TransitionToTimeUntilInstanceAllowedRegistry =
        TransitionToTimeUntilInstanceAllowedMarking()
    override val transitionToPlacesByObjectTypeIndexRegistry: TransitionToPlacesByObjectTypeIndexRegistry =
        TransitionToPlacesByObjectTypeIndexRegistry(
            transitionsRegistry = ocNet.transitionsRegistry,
            placeRegistry = ocNet.placeRegistry,
            placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry,
        )
}
