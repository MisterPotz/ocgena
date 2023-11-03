package simulation.binding

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.ocgena.simulation.interactors.typea.TIOutputPlaceToObjectMarkingTypeAResolver
import javax.inject.Inject
import javax.inject.Provider

interface BindingOutputMarkingResolverFactory {
    fun create(): TIOutputPlacesResolverInteractor
}

class BindingOutputMarkingResolverFactoryImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val bindingOutputMarkingTypeAResolverProvider: Provider<TIOutputPlaceToObjectMarkingTypeAResolver>,
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
