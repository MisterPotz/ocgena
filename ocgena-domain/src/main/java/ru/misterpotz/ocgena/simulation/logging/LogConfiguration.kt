package ru.misterpotz.ocgena.simulation.logging

import simulation.client.OcelLogConfiguration

class LogConfiguration(
    val currentStateLog: CurrentStateLogConfiguration,
    val transitionsLog: TransitionsLogConfiguration,
    val ocelLogConfiguration: OcelLogConfiguration,
    val loggingEnabled : Boolean = true,
)