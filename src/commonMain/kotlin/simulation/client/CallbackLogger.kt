package simulation.client

import model.Time

class CallbackLogger(
    private val simTaskClientCallback: SimTaskClientCallback
) : StubLogger() {
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
