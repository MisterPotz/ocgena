package simulation

import model.OcNetType
import simulation.random.TokenSelector
import simulation.typea.ObjectTokenMoverTypeA

class ObjectTokenMoverFactory(private val tokenSelector: TokenSelector, private val  ocNetType : OcNetType) {
    fun create(): ObjectTokenMover {
        return when(ocNetType) {
            OcNetType.TYPE_A -> ObjectTokenMoverTypeA(tokenSelector)
            OcNetType.TYPE_L -> TODO()
        }
    }
}
