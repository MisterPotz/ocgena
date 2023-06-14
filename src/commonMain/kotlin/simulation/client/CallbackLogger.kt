package simulation.client

import model.Time
import simulation.client.loggers.DefaultLogger

class CallbackLogger(
    private val simTaskClientCallback: SimTaskClientCallback
) : DefaultLogger() {
    override fun onStart() {
        simTaskClientCallback.onExecutionStart()
    }
    override fun onEnd() {
        simTaskClientCallback.onExecutionFinish()
    }

    override fun onTimeout() {
        simTaskClientCallback.onExecutionFinish()
    }

    override fun onTimeShift(delta: Time) = Unit
}
