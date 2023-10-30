package ru.misterpotz.simulation.client.loggers

import ru.misterpotz.simulation.logging.LoggingEvent

interface StepAggregatingLogReceiver {
    fun onEvent(loggingEvent: LoggingEvent)
}