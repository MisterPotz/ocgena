package ru.misterpotz.ocgena.simulation.logging.loggers

abstract class Writer {
    abstract fun writeLine(line: String)

    abstract fun end()
}