package ru.misterpotz.ocgena.simulation_old.logging


class LogConfiguration(
    val currentStateLog: CurrentStateLogConfiguration,
    val transitionsLog: TransitionsLogConfiguration,
    val loggingEnabled: Boolean = true,
) {
    companion object {
        fun default() : LogConfiguration {
            return LogConfiguration(
                currentStateLog = CurrentStateLogConfiguration(
                    includeOngoingTransitions = true,
                    includeNextTransitionAllowedTiming = true,
                    includePlaceMarking = true
                ),
                transitionsLog = TransitionsLogConfiguration(
                    includeEndingTransitions = true,
                    includeStartingTransitions = true
                ),
                loggingEnabled = true
            )
        }
    }
}