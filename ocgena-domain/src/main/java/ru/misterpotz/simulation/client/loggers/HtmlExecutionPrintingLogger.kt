package simulation.client.loggers

import model.*
import ru.misterpotz.model.ObjectMarking
import ru.misterpotz.simulation.logging.LogConfiguration
import simulation.Logger
import ru.misterpotz.simulation.structure.SimulatableComposedOcNet
import ru.misterpotz.simulation.state.SimulationTime
import simulation.client.Writer
import utils.html.bold
import utils.html.color
import utils.html.indentLinesRoot
import utils.print


class HtmlExecutionPrintingLogger(
    override val loggingEnabled: Boolean,
    val loggingConfiguration: LogConfiguration,
    val labelMapping: LabelMapping,
    val writer: Writer,
) : Logger {

    fun write(string: String) {
        writer.writeLine(string)
    }

    fun write(lines: List<String>) {
        for (line in lines) {
            writer.writeLine(line)
        }
    }

    override fun onStart() {
        write(indentLinesRoot(0, bold("execution started")))
    }

    override fun onInitialMarking(marking: ObjectMarking) {
        val lines = marking.toLines()
        write(indentLinesRoot(1, lines, marginSymbol = "#"))
    }

    override fun onFinalMarking(marking: ObjectMarking) {
        val lines = marking.toLines()
        write(indentLinesRoot(0, color(bold("final marking"), backgroundColor = "rgb(239, 166, 166)")))
        write(
            indentLinesRoot(
                1,
                lines,
                marginSymbol = color("#", backgroundColor = "rgb(239, 166, 166)"),
            )
        )
    }

    override fun onEnd() {
        write(indentLinesRoot(0, bold("execution ended")))
        writer.end()
    }

    override fun onTimeout() {
        write(indentLinesRoot(0, bold("execution timeout")))
    }

    override fun onTimeShift(delta: Time) {
        val timeText = utils.html.color(
            "+${delta.print()}", backgroundColor = "rgb(102, 3, 132)",
            fontColor = "rgb(209, 168, 208)"
        )
        val onTimeShift = utils.html.color("on time shift $timeText", fontColor = "rgb(137, 89, 130)")

        write(
            indentLinesRoot(
                indentation = 2,
                listOf(onTimeShift),
                marginSymbol = "",
            ),
        )
    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime
    ) {
        write(
            indentLinesRoot(
                indentation = 1,
                item = color(
                    "execution step: $stepIndex",
                    backgroundColor = "rgb(0, 95, 135)",
                    fontColor = "rgb(0, 240, 234)",
                )
            )
        )
        write(
            indentLinesRoot(
                indentation = 2,
                color(
                    color(
                        simulationTime.toString(),
                        backgroundColor = "rgb(104, 0, 255)",
                    ),
                    fontColor = "rgb(255, 215, 95)",
                )
            )
        )
        write(indentLinesRoot(indentation = 2, bold("current state: ")))

        write(indentLinesRoot(indentation = 3, state.toHtmlLines(), marginSymbol = "*"))
    }

    override fun onTransitionEndSectionStart() {
        write(indentLinesRoot(2, bold("ending transitions:")))
    }

    override fun beforeStartingNewTransitions() {
        write(indentLinesRoot(2, bold("starting transitions:")))
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        write(
            indentLinesRoot(
                3,
                executedBinding.prettyPrintHtmlLinesExecuted(),
                marginSymbol = "x"
            )
        )
    }

    override fun onStartTransition(transition: ActiveFiringTransition) {
        write(
            indentLinesRoot(
                3,
                transition.prettyPrintHtmlLinesStarted()
            )
        )
    }
}
