package ru.misterpotz.simulation.logging.loggers

import model.Arcs
import model.PlaceTyping
import model.Transitions
import ru.misterpotz.marking.transitions.TransitionInstancesMarking
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarking
import ru.misterpotz.marking.objects.Time
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.state.SimulationStepState
import ru.misterpotz.simulation.state.SimulationTime
import ru.misterpotz.simulation.structure.RunningSimulatableOcNet
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.marking.transitions.TransitionTimesMarking
import ru.misterpotz.simulation.queue.TokenGenerationFacade
import simulation.SimulationStateProvider
import javax.inject.Inject

interface CurrentSimulationDelegate {
    val currentStep: Long
    val simGlobalTime: Time
    val simTime: SimulationTime
    val pMarking: ObjectMarking
    val ocNet: SimulatableComposedOcNet<*>
    val initialMarking: ImmutableObjectMarking
    val state: SimulatableComposedOcNet.State
    val simulationStepState: SimulationStepState
    val tTimesMarking: TransitionTimesMarking
    val runningSimulatableOcNet: RunningSimulatableOcNet
    val tMarking: TransitionInstancesMarking
    val transitions: Transitions
    val placeTyping: PlaceTyping
    val tokenGenerationFacade: TokenGenerationFacade
    val arcs : Arcs
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
    override val transitions get() = ocNet.coreOcNet.transitions
    override val placeTyping: PlaceTyping get() = ocNet.coreOcNet.placeTyping
    override val tokenGenerationFacade: TokenGenerationFacade get() = _tokenGenerationFacade
    override val arcs: Arcs get() = ocNet.coreOcNet.arcs
}
