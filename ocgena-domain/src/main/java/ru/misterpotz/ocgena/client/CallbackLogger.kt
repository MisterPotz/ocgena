package simulation.client

import ru.misterpotz.marking.objects.Time
import simulation.client.loggers.NoOpLogger

class CallbackLogger(
    private val simTaskClientCallback: SimTaskClientCallback
) : NoOpLogger() {
    override fun onStart() {
        simTaskClientCallback.onExecutionStart()
    }
    override fun onEnd() {
        simTaskClientCallback.onExecutionFinish()
    }

    override fun onTimeout() {
        simTaskClientCallback.onExecutionFinish()
    }

    fun onTimeShift(delta: Time) = Unit
}
