package simulation

import eventlog.EventLog
import eventlog.ModelToEventLogConverter
import model.*
import utils.*

class DebugTracingLogger(
    val logCurrentState: Boolean = false,
    private val labelsActivities: LabelsActivities,
) : Logger {
    private val eventLog = EventLog()
    private val modelToEventLogConverter = ModelToEventLogConverter(
        labelsActivities = labelsActivities,
    )
    override val loggingEnabled: Boolean
        get() = true

    override fun onTimeout() {
        mprintln("execution timeout")
    }

    override fun onStart() {
        mprintln("execution started")
    }

    override fun onFinalMarking(marking: ObjectMarking) {
        mprintln("""${background("125")}final marking:$ANSI_RESET""".indent(1))
        mprintln(marking.toString().indentMargin(2, "${
            background("125")}#$ANSI_RESET"))
    }

    override fun onInitialMarking(marking: ObjectMarking) {
        mprintln("""${
            background("23")}initial marking:$ANSI_RESET""".indent(1))
        mprintln(marking.toString().indentMargin(2, "#"))
    }

    override fun onEnd() {
        mprintln("execution ended, have recorded ${eventLog.events.size} events")
    }

    override fun onTimeShift(delta: Time) {
        mprintln("${font(ANSI_PINK)}on time shift ${background("128")}+${ delta.print()}$ANSI_RESET".indent(2))
    }

    override fun onExecutionStepStart(
        stepIndex: Int,
        state: SimulatableComposedOcNet.State,
        simulationTime: SimulationTime,
    ) {
        mprintln("${background("24")}${font("51")}execution step: $stepIndex$ANSI_RESET".indent(1))
        mprintln("""${font(ANSI_ORANGE)}time: ${background("57")}$simulationTime$ANSI_RESET""".indent(2))
        mprintln("""current state: """.indent(2, prefix = ""))
        mprintln(state.toString().indentMargin(3, margin = "*"))
    }

    override fun onTransitionEndSectionStart() {
        mprintln("""ending transitions:""".indent(2))
    }

    override fun onTransitionStartSectionStart() {
        mprintln("""starting transitions:""".indent(2))
    }

    override fun onTransitionEnded(executedBinding: ExecutedBinding) {
        val event = modelToEventLogConverter.executedToEvent(executedBinding)
        eventLog.recordEvent(event)
        mprintln(executedBinding.prettyPrintExecuted().indentMargin(3, margin = "x"))
    }

    override fun onTransitionStart(transition: ActiveFiringTransition) {
        mprintln(transition.prettyPrintStarted().trimMargin().indentMargin(3))
    }
}