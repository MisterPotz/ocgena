package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.simulation.continuation.ExecutionContinuation

interface StepExecutor {
    suspend fun executeStep(executionContinuation: ExecutionContinuation)
}