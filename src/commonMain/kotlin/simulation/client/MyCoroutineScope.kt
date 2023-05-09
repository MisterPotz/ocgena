package simulation.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class MyCoroutineScope(coroutineContext: CoroutineContext = Dispatchers.Main) : CoroutineScope {
    val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = job + coroutineContext
}
