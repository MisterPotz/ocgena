package ru.misterpotz.ocgena.simulation.logging.loggers

import net.mamoe.yamlkt.Yaml
import ru.misterpotz.ocgena.collections.TransitionInstance
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.binding.ExecutedBinding
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.utils.ExecutedBindingDebugPrinter
import ru.misterpotz.ocgena.utils.TransitionInstanceDebugPrinter
import simulation.client.loggers.NoOpLogger
import utils.*
import javax.inject.Inject

class ANSIDebugTracingLogger @Inject constructor(
    private val currentSimulationDelegate: CurrentSimulationDelegate,
    private val simulationConfig: SimulationConfig,
    val yaml: Yaml,
    val developmentDebugConfig: DevelopmentDebugConfig,
    val transitionInstanceDebugPrinter: TransitionInstanceDebugPrinter,
    val executedBindingDebugPrinter: ExecutedBindingDebugPrinter,
    val writer: Writer,
) : NoOpLogger(), CurrentSimulationDelegate by currentSimulationDelegate {
    private fun dumpState(): String {
        // TODO: provide real state
        return ""
//        return yaml.encodeToString(
//            SerializableSimulationState(
//                simGlobalTime,
//                state.toSerializable()
//            )
//        )
//            .replace(Regex("\\n[\\s\\r]*\\n"), "\n")
    }

    private fun dumpInput(): String {
        return yaml.encodeToString(simulationConfig).replace(Regex("\\n[\\s\\r]*\\n"), "\n")
    }

    override fun onStart() {
        writer.writeLine("execution started")

        // always dump net with
        if (developmentDebugConfig.dumpState) {
            println("onStart dump net: ${dumpInput().replace("\n", "\n\r")}")
        }


        if (developmentDebugConfig.dumpState) {
            println("onStart dump state: ${dumpState().replace("\n", "\n\r")}")
        }
    }

    override fun afterInitialMarking() {
        writer.writeLine(
            """${
                background("23")
            }initial marking:$ANSI_RESET""".indent(1, "\t")
        )
        writer.writeLine(pMarking.toString().indentMargin(2, "#"))
    }

    override fun onExecutionNewStepStart() {
        if (developmentDebugConfig.dumpState) {
            println(
                "\r\ndump after step state: ${simulationStepState.currentStep}: \r\n${
                    dumpState().replace(
                        "\n",
                        "\r\n"
                    )
                }"
            )
        }
        writer.writeLine("${background("24")}${font("51")}execution step: $currentStep".indent(1, "\t"))
        writer.writeLine("""${font(ANSI_ORANGE)}time: ${background("57")}$simGlobalTime""".indent(2, "\t"))
        writer.writeLine("""current state: """.indent(2, prefix = ""))
        // TODO: output ongoing transitions in a state as well
        writer.writeLine(pMarking.toString().indentMargin(3, margin = "*"))
    }

    override fun beforeStartingNewTransitions() {
        writer.writeLine("""starting transitions:""".indent(2, "\t"))
    }

    override fun onStartTransition(transition: TransitionInstance) {
        with(transitionInstanceDebugPrinter) {
            writer.writeLine(prettyPrintStarted(transition).trimMargin().indentMargin(3))
        }
    }

    override fun beforeEndingTransitions() {
        writer.writeLine("""ending transitions:""".indent(2, "\t"))
    }

    override fun onEndTransition(executedBinding: ExecutedBinding) {
        with(executedBindingDebugPrinter) {
            writer.writeLine(prettyPrintExecuted(executedBinding).indentMargin(3, margin = "x"))
        }
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
            pMarking.toString().indentMargin(
                2, "${
                    background("125")
                }#$ANSI_RESET"
            )
        )
        if (developmentDebugConfig.dumpState) {
            println("onFinish dump state: ${dumpState()}")
        }
    }

    override fun onTimeout() {
        writer.writeLine("execution timeout")
    }

    override fun onEnd() {
        writer.writeLine("execution ended")
    }
}
