package ru.misterpotz.ocgena.simulation_old.logging.loggers

abstract class Writer {
    abstract fun writeLine(line: String)

    abstract fun end()
}