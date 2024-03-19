package simulation.binding

import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.ocgena.simulation.interactors.TIOutputPlacesResolverInteractorImpl
import javax.inject.Inject
import javax.inject.Provider

interface BindingOutputMarkingResolverFactory {
    fun create(): TIOutputPlacesResolverInteractor
}

class BindingOutputMarkingResolverFactoryImpl @Inject constructor(
    private val simulationConfig: SimulationConfig,
    private val bindingOutputMarkingTypeAResolverProvider: Provider<TIOutputPlacesResolverInteractorImpl>,
) : BindingOutputMarkingResolverFactory {
    private val ocNetType get() = simulationConfig.ocNetType

    override fun create(): TIOutputPlacesResolverInteractor {
        return bindingOutputMarkingTypeAResolverProvider.get()
    }
}
