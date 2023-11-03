package ru.misterpotz.ocgena.simulation.logging.loggers

import ru.misterpotz.ocgena.simulation.logging.LoggingEvent

interface StepAggregatingLogReceiver {
    fun onEvent(loggingEvent: LoggingEvent)
}