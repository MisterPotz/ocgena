package simulation.binding

import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.interactors.TIOutputPlacesResolverInteractor
import ru.misterpotz.ocgena.simulation_old.interactors.TIOutputPlacesResolverInteractorImpl
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
