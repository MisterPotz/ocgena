package ru.misterpotz.ocgena.simulation.state

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.di.SimulationScope
import ru.misterpotz.ocgena.simulation.structure.State
import javax.inject.Inject

@SimulationScope
class PMarkingProvider @Inject constructor() {
    init {
        println("pmarking provider created")
    }

    private val placeToObjectMarking by lazy(LazyThreadSafetyMode.NONE) {
        println("initting marking")
        PlaceToObjectMarking()
    }

    fun get(): PlaceToObjectMarking {
        return placeToObjectMarking
    }

    override fun toString(): String {
        return "PMarkingProvider(placeToObjectMarking=$placeToObjectMarking)"
    }
}

@SimulationScope
class StateImpl @Inject constructor(
    ocNet: OCNet,
    override val arcsMultiplicityRegistry: ArcsMultiplicityRegistry,
    val pMarkingProvider: PMarkingProvider,
) : State {
//    override val tMarking: TransitionToInstancesRegistryOriginal = TransitionToInstancesRegistryOriginal()
    override val pMarking: PlaceToObjectMarking = pMarkingProvider.get()
//    override val tTimesMarking: TransitionToTimeUntilInstanceAllowedRegistryOriginal =
//        TransitionToTimeUntilInstanceAllowedMarking()
    override val transitionToPlacesByObjectTypeIndexRegistry: TransitionToPlacesByObjectTypeIndexRegistry =
        TransitionToPlacesByObjectTypeIndexRegistry(
            transitionsRegistry = ocNet.transitionsRegistry,
            placeRegistry = ocNet.placeRegistry,
            placeToObjectTypeRegistry = ocNet.placeToObjectTypeRegistry,
        )
}
