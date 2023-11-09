package ru.misterpotz.ocgena.simulation_old.state

import ru.misterpotz.ocgena.registries.ArcsRegistry
import ru.misterpotz.ocgena.simulation_old.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation_old.SimulationStateProvider
import ru.misterpotz.ocgena.simulation_old.structure.State
import javax.inject.Inject


interface CurrentSimulationDelegate : OCNet {
    val ocNet: SimulatableOcNetInstance
    val state: State
    val currentStep: Long
    val simulationStepState: SimulationStepState
    val simGlobalTime: Time
    val simTime: SimulationTime
    val initialMarkingScheme: MarkingScheme?
    val newTokenGenerationFacade: NewTokenGenerationFacade
    val pMarking: PlaceToObjectMarking
        get() = state.pMarking

    override val transitionsRegistry: TransitionsRegistry
        get() = ocNet.transitionsRegistry
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
        get() = ocNet.placeToObjectTypeRegistry
    override val petriAtomRegistry : PetriAtomRegistry
        get() = ocNet.petriAtomRegistry
    override val arcsRegistry : ArcsRegistry
        get() = ocNet.arcsRegistry
    override val objectTypeRegistry: ObjectTypeRegistry
        get() = ocNet.objectTypeRegistry
    override val placeTypeRegistry: PlaceTypeRegistry
        get() = ocNet.placeTypeRegistry
    override val placeRegistry: PlaceRegistry
        get() = ocNet.placeRegistry
    override val inputPlaces: PlaceRegistry
        get() = ocNet.inputPlaces
    override val outputPlaces: PlaceRegistry
        get() = ocNet.outputPlaces
}

class CurrentSimulationDelegateImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val simulationStateProvider: SimulationStateProvider,
    override val newTokenGenerationFacade: NewTokenGenerationFacade
) :
    CurrentSimulationDelegate {

    override val ocNetType: OcNetType = simulationConfig.ocNetType
    override val currentStep get() = simulationStateProvider.getSimulationStepState().currentStep
    override val simGlobalTime get() = simulationStateProvider.getSimulationTime().globalTime
    override val simTime get() = simulationStateProvider.getSimulationTime()
    override val ocNet get() = simulationStateProvider.simulatableOcNetInstance()
    override val initialMarkingScheme get() = simulationConfig.initialMarking
    override val state get() = simulationStateProvider.simulatableOcNetInstance().state
    override val simulationStepState get() = simulationStateProvider.getSimulationStepState()
}
