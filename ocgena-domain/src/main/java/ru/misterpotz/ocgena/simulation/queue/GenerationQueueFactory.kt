package ru.misterpotz.ocgena.simulation.queue

import ru.misterpotz.ocgena.simulation.config.SimulationConfig

interface GenerationQueueFactory {
    fun createGenerationQueue(simulationConfig: SimulationConfig): GenerationQueue
}