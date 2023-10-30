package simulation.binding

import model.OcNetType
import ru.misterpotz.simulation.config.SimulationConfig
import simulation.ObjectTokenMoverFactory
import simulation.typea.BindingOutputMarkingTypeAResolver
import javax.inject.Inject

interface BindingOutputMarkingResolverFactory {
    fun create(): InputToOutputPlaceResolver
}

class BindingOutputMarkingResolverFactoryImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val objectTokenMoverFactory: ObjectTokenMoverFactory
) : BindingOutputMarkingResolverFactory {
    private val ocNet = simulationConfig.templateOcNet
    private val ocNetType get() = simulationConfig.ocNetType
    private val arcs get() = ocNet.coreOcNet.arcs
    private val placeTyping get() = ocNet.coreOcNet.placeTyping
    private val objectTokenGenerator get() = simulationConfig.objectTokenGenerator

    override fun create(): InputToOutputPlaceResolver {
        return when (ocNetType) {
            OcNetType.AALST -> BindingOutputMarkingTypeAResolver(
                arcs = arcs,
                placeTyping = placeTyping,
                objectTokenGenerator = objectTokenGenerator,
                objectTokenMoverFactory = objectTokenMoverFactory
            )

            OcNetType.LOMAZOVA -> TODO("I.A.Lomazova specification is yet to be done")
        }
    }
}
