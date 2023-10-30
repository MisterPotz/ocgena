package ru.misterpotz.simulation.logging

enum class LogEvent {
    SIMULATION_START,
    STARTED_TRANSITIONS,
    ENDED_TRANSITIONS,
    CURRENT_STATE_PMARKING_REPORTED,
    CURRENT_STATE_TRANSITIONS_ALLOWED_TIME_REPORTED,
    CURRENT_STATE_TMARKING_REPORTED,
    FINAL_STATE_REPORTED,
    SIM_TIME_SHIFT_REPORTED,
    SIMULATION_END
}