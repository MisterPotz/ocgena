package ru.misterpotz.ocgena.simulation_old.logging.loggers

import javax.inject.Inject

class DevelopmentLogWriter @Inject constructor() : Writer() {
    override fun writeLine(line: String) {
        println(line)
    }

    override fun end() {
        println()
    }
}