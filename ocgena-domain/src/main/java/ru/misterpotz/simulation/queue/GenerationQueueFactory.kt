package ru.misterpotz.simulation.queue

import ru.misterpotz.simulation.config.SimulationConfig

interface GenerationQueueFactory {
    fun createGenerationQueue(simulationConfig: SimulationConfig): GenerationQueue
}