package ru.misterpotz.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TasksRegistry(private val scope: CoroutineScope) {
    private val tasksRegistry: MutableMap<Long, Task> = mutableMapOf()
    private val resultsRegistry: MutableMap<Long, TaskResult> = mutableMapOf()
    private var indexIssuer: Long = 0L
    private val mutex = Mutex()

    suspend fun getResults(handle: Long): TaskResult? {
        return mutex.withLock {
            resultsRegistry[handle]
        }
    }

    suspend fun launch(task: Task): Result<Long> {
        val work = task.workFactory.getOrThrow()

        val newIndex = mutex.withLock {
            val new = indexIssuer++
            tasksRegistry[new] = task
            new
        }

        scope.launch {
            flow<Unit> {
                work.run()
                println("work ended successfully")
            }.onCompletion { exception ->
                exception?.printStackTrace()
                mutex.withLock {
                    tasksRegistry.remove(newIndex)
                    resultsRegistry[newIndex] = TaskResult(exception)
                }
            }.collect()
        }

        return Result.success(newIndex)
    }

    suspend fun interruptSimulationAndRemove(handle: Long) {
        return mutex.withLock {
            val value = tasksRegistry[handle]
            value?.finish()
            tasksRegistry.remove(handle)
        }
    }

//    suspend fun setSimulationAsCompleted(handle: Long, result: SimulationCompletionResult) {
//        mutex.withLock {
//            resultsRegistry[handle] = result
//        }
//    }

    suspend fun getTaskResult(handle: Long): TaskResult? {
        return mutex.withLock {
            resultsRegistry[handle]
        }
    }

    interface Work {
        suspend fun run()
        suspend fun destroy()
    }

    class Task(
        val workFactory: Result<Work>,
    ) {
        suspend fun finish() {
            workFactory.getOrThrow().destroy()
        }
    }

    data class TaskResult(
        val exception: Throwable?,
    ) {
        val isOk: Boolean
            get() = exception == null
    }

}
