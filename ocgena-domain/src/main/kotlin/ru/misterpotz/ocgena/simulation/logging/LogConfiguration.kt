package ru.misterpotz.ocgena.simulation.logging


class LogConfiguration(
    val currentStateLog: CurrentStateLogConfiguration,
    val transitionsLog: TransitionsLogConfiguration,
    val loggingEnabled: Boolean = true,
)