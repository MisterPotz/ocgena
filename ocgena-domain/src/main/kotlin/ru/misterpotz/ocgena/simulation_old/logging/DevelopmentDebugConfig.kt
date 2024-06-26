package ru.misterpotz.ocgena.simulation_old.logging

data class DevelopmentDebugConfig(
    val dumpEndStateMarking : Boolean = false,
    val dumpTimePNTransitionMarking: Boolean = false,
    val developmentLoggersEnabled : Boolean = true,
    val dumpState: Boolean = false,
    val dumpConsistencyCheckLogs: Boolean = false,
    val markEachNStep : Boolean = true,
    val stepNMarkGranularity : Int = 1000
)

fun fastNoDevSetup() : DevelopmentDebugConfig {
    return DevelopmentDebugConfig(
        developmentLoggersEnabled = false,
        dumpState = false,
        dumpConsistencyCheckLogs = false,
        markEachNStep = false
    )
}

fun fastConsistencyDevSetup() : DevelopmentDebugConfig {
    return DevelopmentDebugConfig(
        developmentLoggersEnabled = false,
        dumpState = false,
        dumpConsistencyCheckLogs = true,
        markEachNStep = false
    )
}

fun fastFullDev() : DevelopmentDebugConfig {
    return DevelopmentDebugConfig(
        developmentLoggersEnabled = true,
        dumpState = true,
        dumpConsistencyCheckLogs = false,
        markEachNStep = true
    )
}