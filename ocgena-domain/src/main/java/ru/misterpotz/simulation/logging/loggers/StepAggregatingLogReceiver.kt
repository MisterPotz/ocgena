package ru.misterpotz.simulation.logging.loggers

import ru.misterpotz.simulation.logging.LoggingEvent

interface StepAggregatingLogReceiver {
    fun onEvent(loggingEvent: LoggingEvent)
}