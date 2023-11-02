package ru.misterpotz.ocgena.simulation.logging.loggers

import model.ArcsRegistry
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry
import model.TransitionsRegistry
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking
import ru.misterpotz.ocgena.collections.objects.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionInstancesMarking
import ru.misterpotz.ocgena.collections.transitions.TransitionTimesMarking
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.queue.TokenGenerationFacade
import ru.misterpotz.ocgena.simulation.state.SimulationStepState
import ru.misterpotz.ocgena.simulation.state.SimulationTime
import ru.misterpotz.ocgena.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.ocgena.simulation.structure.SimulatableComposedOcNet
import simulation.SimulationStateProvider
import javax.inject.Inject

interface CurrentSimulationDelegate {
    val currentStep: Long
    val simGlobalTime: Time
    val simTime: SimulationTime
    val pMarking: PlaceToObjectMarking
    val ocNet: SimulatableComposedOcNet<*>
    val initialMarking: ImmutableObjectMarking
    val state: SimulatableComposedOcNet.State
    val simulationStepState: SimulationStepState
    val tTimesMarking: TransitionTimesMarking
    val runningSimulatableOcNet: RunningSimulatableOcNet
    val tMarking: TransitionInstancesMarking
    val transitionsRegistry: TransitionsRegistry
    val placeObjectTypeRegistry: PlaceObjectTypeRegistry
    val tokenGenerationFacade: TokenGenerationFacade
    val arcsRegistry : ArcsRegistry
}

class CurrentSimulationDelegateImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val simulationStateProvider: SimulationStateProvider,
    private val _tokenGenerationFacade: TokenGenerationFacade
) :
    CurrentSimulationDelegate {
    override val currentStep get() = simulationStateProvider.getSimulationStepState().currentStep
    override val simGlobalTime get() = simulationStateProvider.getSimulationTime().globalTime
    override val simTime get() = simulationStateProvider.getSimulationTime()
    override val tTimesMarking get() = simulationStateProvider.getOcNetState().tTimesMarking
    override val tMarking get() = simulationStateProvider.getOcNetState().tMarking
    override val pMarking get() = simulationStateProvider.getOcNetState().pMarking
    override val ocNet get() = simulationConfig.templateOcNet
    override val initialMarking get() = simulationConfig.initialMarking
    override val state get() = simulationStateProvider.getOcNetState()
    override val simulationStepState get() = simulationStateProvider.getSimulationStepState()
    override val runningSimulatableOcNet get() = simulationStateProvider.runningSimulatableOcNet()
    override val transitionsRegistry get() = ocNet.coreOcNet.transitions
    override val placeObjectTypeRegistry: PlaceObjectTypeRegistry get() = ocNet.coreOcNet.placeTyping
    override val tokenGenerationFacade: TokenGenerationFacade get() = _tokenGenerationFacade
    override val arcsRegistry: ArcsRegistry get() = ocNet.coreOcNet.arcs
}
