package simulation.binding

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.api.interactors.TIOutputPlacesResolverInteractor
import simulation.typea.TIOutputMarkingTypeAResolver
import javax.inject.Inject
import javax.inject.Provider

interface BindingOutputMarkingResolverFactory {
    fun create(): TIOutputPlacesResolverInteractor
}

class BindingOutputMarkingResolverFactoryImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val bindingOutputMarkingTypeAResolverProvider: Provider<TIOutputMarkingTypeAResolver>,
) : BindingOutputMarkingResolverFactory {
    private val ocNetType get() = simulationConfig.ocNetType

    override fun create(): TIOutputPlacesResolverInteractor {
        return when (ocNetType) {
            OcNetType.AALST -> {
                bindingOutputMarkingTypeAResolverProvider.get()
            }
            OcNetType.LOMAZOVA -> TODO("I.A.Lomazova specification is yet to be done")
        }
    }
}
