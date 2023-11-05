package ru.misterpotz.ocgena.simulation.logging

class DevelopmentDebugConfig(
    val developmentLoggersEnabled : Boolean = true,
    val dumpState: Boolean = false,
    val dumpConsistencyCheckLogs: Boolean = false,
)

fun fastNoDevSetup() : DevelopmentDebugConfig {
    return DevelopmentDebugConfig(
        developmentLoggersEnabled = false,
        dumpState = false,
        dumpConsistencyCheckLogs = false
    )
}

fun fastConsistencyDevSetup() : DevelopmentDebugConfig {
    return DevelopmentDebugConfig(
        developmentLoggersEnabled = false,
        dumpState = false,
        dumpConsistencyCheckLogs = true
    )
}