package simulation

import model.OcNetType
import simulation.typea.LockedTokensMoverTypeA
import javax.inject.Inject
import javax.inject.Provider

interface ObjectTokenMoverFactory {
    fun create() : LockedTokensMover
}

class ObjectTokenMoverFactoryImpl @Inject constructor(
    private val ocNetType: OcNetType,
    private val tokenMoverTypeAProvider : Provider<LockedTokensMoverTypeA>,
) : ObjectTokenMoverFactory {
    override fun create(): LockedTokensMover {
        return when (ocNetType) {
            OcNetType.AALST -> tokenMoverTypeAProvider.get()
            OcNetType.LOMAZOVA -> TODO()
        }
    }
}
