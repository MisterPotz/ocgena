package simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import ru.misterpotz.model.marking.ObjectMarking
import ru.misterpotz.model.marking.Time
import ru.misterpotz.simulation.logging.LogConfiguration
import simulation.Logger
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.state.SimulationTime
import simulation.client.Writer
import utils.*

class ANSITracingLogger(
    override val loggingEnabled: Boolean,
    val loggingConfiguration: LogConfiguration,
    val writer: Writer,
) : Logger {


    fun onStart() {

    }
    fun afterInitialMarking()

    fun onExecutionNewStepStart()

    fun beforeStartingNewTransitions()
    fun onStartTransition(transition: ActiveFiringTransition)
    fun afterStartingNewTransitions()

    fun beforeEndingTransitions()
    fun onEndTransition(executedBinding: ExecutedBinding)
    fun afterEndingTransitions()

    fun onExecutionStepFinish(newTimeDelta: Time)

    fun afterFinalMarking()

    fun onTimeout()

    fun onEnd()

    override fun onTimeout() {
        writer.writeLine("execution timeout")
    }

    override fun onStart() {
        writer.writeLine("execution started")
    }

    override fun onFinalMarking(marking: ObjectMarking) {
        writer.writeLine("""${background("125")}final marking:$ANSI_RESET""".indent(1))
        writer.writeLine(
            marking.toString().indentMargin(
                2, "${
                    background("125")
                }#$ANSI_RESET"
            )
        )
    }

    override fun onInitialMarking(marking: ObjectMarking) {
        writer.writeLine(
            """${
                background("23")
            }initial marking:$ANSI_RESET""".indent(1)
        )
        writer.writeLine(marking.toString().indentMargin(2, "#"))
    }

    override fun onEnd() {
        writer.writeLine("execution ended")
    }

    override fun onTimeShift(delta: Time) {
        writer.writeLine("${font(ANSI_PINK)}on time shift ${background("128")}+${delta.print()}$ANSI_RESET".indent(2))
    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime,
    ) {
        writer.writeLine("${background("24")}${font("51")}execution step: $stepIndex$ANSI_RESET".indent(1))
        writer.writeLine("""${font(ANSI_ORANGE)}time: ${background("57")}$simulationTime$ANSI_RESET""".indent(2))
        writer.writeLine("""current state: """.indent(2, prefix = ""))
        writer.writeLine(state.toString().indentMargin(3, margin = "*"))
    }

    override fun onTransitionEndSectionStart() {
        writer.writeLine("""ending transitions:""".indent(2))
    }

    override fun beforeStartingNewTransitions() {
        writer.writeLine("""starting transitions:""".indent(2))
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        writer.writeLine(executedBinding.prettyPrintExecuted().indentMargin(3, margin = "x"))
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
        writer.writeLine(transition.prettyPrintStarted().trimMargin().indentMargin(3))
    }
}
