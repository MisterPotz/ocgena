package simulation

import model.OcNetType
import simulation.random.TokenSelector
import simulation.typea.ObjectTokenMoverTypeA
import javax.inject.Inject

interface ObjectTokenMoverFactory

class ObjectTokenMoverFactoryImpl @Inject constructor(
    private val tokenSelector: TokenSelector,
    private val ocNetType: OcNetType
) {
    fun create(): ObjectTokenMover {
        return when (ocNetType) {
            OcNetType.AALST -> ObjectTokenMoverTypeA(tokenSelector)
            OcNetType.LOMAZOVA -> TODO()
        }
    }
}
