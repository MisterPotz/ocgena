package ru.misterpotz.ocgena.simulation.logging.loggers

import model.ArcsRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.TransitionsRegistry
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToInstancesMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionToTimeUntilInstanceAllowedMarking
import ru.misterpotz.ocgena.registries.PetriAtomRegistry
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerationFacade
import ru.misterpotz.ocgena.simulation.state.SimulationStepState
import ru.misterpotz.ocgena.simulation.state.SimulationTime
import ru.misterpotz.ocgena.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.ocgena.simulation.structure.OcNetInstance
import simulation.SimulationStateProvider
import javax.inject.Inject

interface CurrentSimulationDelegate {
    val currentStep: Long
    val simGlobalTime: Time
    val simTime: SimulationTime
    val pMarking: PlaceToObjectMarking
    val ocNet: OcNetInstance
    val initialMarkingScheme: MarkingScheme
    val state: OcNetInstance.State
    val simulationStepState: SimulationStepState
    val tTimesMarking: TransitionToTimeUntilInstanceAllowedMarking
    val runningSimulatableOcNet: RunningSimulatableOcNet
    val tMarking: TransitionToInstancesMarking
    val transitionsRegistry: TransitionsRegistry
    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry
    val petriAtomRegistry : PetriAtomRegistry
    val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
    val arcsRegistry : ArcsRegistry
}

class CurrentSimulationDelegateImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val simulationStateProvider: SimulationStateProvider,
    private val _New_tokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade
) :
    CurrentSimulationDelegate {
    override val currentStep get() = simulationStateProvider.getSimulationStepState().currentStep
    override val simGlobalTime get() = simulationStateProvider.getSimulationTime().globalTime
    override val simTime get() = simulationStateProvider.getSimulationTime()
    override val tTimesMarking get() = simulationStateProvider.getOcNetState().tTimesMarking
    override val tMarking get() = simulationStateProvider.getOcNetState().tMarking
    override val pMarking get() = simulationStateProvider.getOcNetState().pMarking
    override val ocNet get() = simulationConfig.templateOcNet
    override val initialMarkingScheme get() = simulationConfig.initialMarking
    override val state get() = simulationStateProvider.getOcNetState()
    override val simulationStepState get() = simulationStateProvider.getSimulationStepState()
    override val runningSimulatableOcNet get() = simulationStateProvider.runningSimulatableOcNet()
    override val transitionsRegistry get() = ocNet.ocNetScheme.transitionsRegistry
    override val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry get() = ocNet.ocNetScheme.placeToObjectTypeRegistry
    override val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade get() = _New_tokenTimeBasedGenerationFacade
    override val arcsRegistry: ArcsRegistry get() = ocNet.ocNetScheme.arcsRegistry
    override val petriAtomRegistry: PetriAtomRegistry get() = simulationStateProvider.getOcNetState().petriAtomRegistry
}
