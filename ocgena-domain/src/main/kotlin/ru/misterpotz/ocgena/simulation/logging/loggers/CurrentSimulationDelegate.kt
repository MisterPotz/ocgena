package ru.misterpotz.ocgena.simulation.logging.loggers

import model.ArcsRegistry
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerationFacade
import ru.misterpotz.ocgena.simulation.state.SimulationStepState
import ru.misterpotz.ocgena.simulation.state.SimulationTime
import ru.misterpotz.ocgena.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance
import ru.misterpotz.ocgena.simulation.SimulationStateProvider
import javax.inject.Inject

interface CurrentSimulationDelegate : OCNet {
    val ocNet: OcNetInstance
    val state: OcNetInstance.State
    val currentStep: Long
    val simulationStepState: SimulationStepState
    val simGlobalTime: Time
    val simTime: SimulationTime
    val initialMarkingScheme: MarkingScheme
    val runningSimulatableOcNet: RunningSimulatableOcNet
    val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
    val tTimesMarking: TransitionToTimeUntilInstanceAllowedMarking
        get() = state.tTimesMarking
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
    override val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
) :
    CurrentSimulationDelegate {
    override val currentStep get() = simulationStateProvider.getSimulationStepState().currentStep
    override val simGlobalTime get() = simulationStateProvider.getSimulationTime().globalTime
    override val simTime get() = simulationStateProvider.getSimulationTime()
    override val ocNet get() = simulationConfig.ocNetInstance
    override val initialMarkingScheme get() = simulationConfig.initialMarking
    override val state get() = simulationStateProvider.getOcNetState()
    override val simulationStepState get() = simulationStateProvider.getSimulationStepState()
    override val runningSimulatableOcNet get() = simulationStateProvider.runningSimulatableOcNet()
}
