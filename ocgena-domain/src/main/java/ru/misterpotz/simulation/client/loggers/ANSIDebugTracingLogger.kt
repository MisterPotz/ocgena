package simulation.client.loggers

import model.ActiveFiringTransition
import model.ExecutedBinding
import ru.misterpotz.model.marking.Time
import ru.misterpotz.simulation.client.loggers.CurrentSimulationDelegate
import simulation.client.Writer
import utils.*
import javax.inject.Inject

class ANSIDebugTracingLogger @Inject constructor(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    val writer: Writer,
) : NoOpLogger(), CurrentSimulationDelegate by currentSimulationDelegate {
    override fun onStart() {
        writer.writeLine("execution started")
    }

    override fun afterInitialMarking() {
        writer.writeLine(
            """${
                background("23")
            }initial marking:$ANSI_RESET""".indent(1, "\t")
        )
        writer.writeLine(currentPMarking.toString().indentMargin(2, "#"))
    }

    override fun onExecutionNewStepStart() {
        writer.writeLine("${background("24")}${font("51")}execution step: $step".indent(1, "\t"))
        writer.writeLine("""${font(ANSI_ORANGE)}time: ${background("57")}$simTime""".indent(2, "\t"))
        writer.writeLine("""current state: """.indent(2, prefix = ""))
        // TODO: output ongoing transitions in a state as well
        writer.writeLine(currentPMarking.toString().indentMargin(3, margin = "*"))
    }

    override fun beforeStartingNewTransitions() {
        writer.writeLine("""starting transitions:""".indent(2, "\t"))
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
        writer.writeLine(transition.prettyPrintStarted().trimMargin().indentMargin(3))
    }

    override fun beforeEndingTransitions() {
        writer.writeLine("""ending transitions:""".indent(2))
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        writer.writeLine(executedBinding.prettyPrintExecuted().indentMargin(3, margin = "x"))
    }

    override fun onExecutionStepFinish(newTimeDelta: Time) {
        writer.writeLine(
            "${font(ANSI_PINK)}on time shift ${background("128")}+${newTimeDelta.print()}$ANSI_RESET".indent(
                2,
                "\t"
            )
        )
    }

    override fun afterFinalMarking() {
        writer.writeLine("""${background("125")}final marking:$ANSI_RESET""".indent(1, "\t"))
        writer.writeLine(
            currentPMarking.toString().indentMargin(
                2, "${
                    background("125")
                }#$ANSI_RESET"
            )
        )
    }

    override fun onTimeout() {
        writer.writeLine("execution timeout")
    }

    override fun onEnd() {
        writer.writeLine("execution ended")
    }
}
