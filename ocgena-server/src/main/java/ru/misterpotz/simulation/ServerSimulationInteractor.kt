package ru.misterpotz.simulation

import ru.misterpotz.ocgena.simulation_v2.di.SimulationV2Interactor
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.Transitions
import java.util.concurrent.atomic.AtomicBoolean

class ServerSimulationInteractor(
    private val defaultBehavior: SimulationV2Interactor
) : SimulationV2Interactor {
    private var requestedToFinish = AtomicBoolean(false)

    suspend fun finish() {
        requestedToFinish.set(true)
    }

    override suspend fun get(timeRange: LongRange): Long {
        return defaultBehavior.get(timeRange)
    }

    override suspend fun get(transitions: Transitions): TransitionWrapper {
        return defaultBehavior.get(transitions)
    }

    override suspend fun isFinish(): Boolean {
        return requestedToFinish.get()
    }

    class Factory : SimulationV2Interactor.Factory {
        var created: ServerSimulationInteractor? = null
            private set

        override fun create(defaultBehavior: SimulationV2Interactor): SimulationV2Interactor {
            return ServerSimulationInteractor(defaultBehavior).also {
                created = it
            }
        }
    }
}