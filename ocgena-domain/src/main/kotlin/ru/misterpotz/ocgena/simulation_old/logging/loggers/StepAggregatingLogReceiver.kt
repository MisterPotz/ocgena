package ru.misterpotz.ocgena.simulation_old.logging.loggers

import ru.misterpotz.ocgena.simulation_old.logging.LoggingEvent

interface StepAggregatingLogReceiver {
    fun onEvent(loggingEvent: LoggingEvent)
}

object NoOpStepAggregatingLogReceiver : StepAggregatingLogReceiver {
    override fun onEvent(loggingEvent: LoggingEvent) {

    }
}