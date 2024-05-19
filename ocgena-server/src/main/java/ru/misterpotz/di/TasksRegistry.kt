package ru.misterpotz.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TasksRegistry(val scope: CoroutineScope) {
    private val simulationsRegistry: MutableMap<Long, Task> = mutableMapOf()
    private val resultsRegistry: MutableMap<Long, SimulationCompletionResult> = mutableMapOf()
    private var indexIssuer: Long = 0L
    private val mutex = Mutex()

    suspend fun getResults(handle: Long): SimulationCompletionResult? {
        return mutex.withLock {
            resultsRegistry[handle]
        }
    }

    suspend fun launch(serverSimulationComponent: ServerSimulationComponent): Result<Long> {
        val component = runCatching {
            serverSimulationComponent.simulationV2Component()
        }
        if (component.isFailure) {
            return component.map { -1 }
        }
        val simulation = runCatching {
            component.getOrThrow().simulation()
        }
        if (simulation.isFailure) {
            return simulation.map { -1 }
        }

        val newIndex = mutex.withLock {
            indexIssuer++
        }
        mutex.withLock {
            simulationsRegistry[newIndex] =
                Task(serverSimulationComponent)
        }

        scope.launch {
            flow<Unit> {
                simulation.getOrThrow().runSimulation()
            }.onCompletion {
                mutex.withLock {
                    simulationsRegistry.remove(newIndex)
                    resultsRegistry[newIndex] = SimulationCompletionResult(it)
                }
            }.collect()
        }

        return Result.success(newIndex)
    }

    suspend fun interruptSimulationAndRemove(handle: Long) {
        return mutex.withLock {
            val value = simulationsRegistry[handle]
            value?.finish()
            simulationsRegistry.remove(handle)
        }
    }

    suspend fun setSimulationAsCompleted(handle: Long, result: SimulationCompletionResult) {
        mutex.withLock {
            resultsRegistry[handle] = result
        }
    }

    suspend fun getSimulationResult(handle: Long): SimulationCompletionResult? {
        return mutex.withLock {
            resultsRegistry[handle]
        }
    }
}

data class SimulationCompletionResult(
    val exception: Throwable?,
) {
    val isOk: Boolean
        get() = exception == null
}

class Task(
    private val component: ServerSimulationComponent,
) {
    suspend fun finish() {
        component.destroyer().destroy()
    }
}