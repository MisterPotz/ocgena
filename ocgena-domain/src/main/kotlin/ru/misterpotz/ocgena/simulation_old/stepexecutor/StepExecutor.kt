package ru.misterpotz.ocgena.simulation_old.stepexecutor

import ru.misterpotz.ocgena.simulation_old.continuation.ExecutionContinuation

interface StepExecutor {
    suspend fun executeStep(executionContinuation: ExecutionContinuation)
}