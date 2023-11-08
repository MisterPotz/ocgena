package ru.misterpotz.ocgena.simulation.interactors.factories

import ru.misterpotz.ocgena.simulation.interactors.EnabledBindingResolverInteractor
import javax.inject.Inject
import javax.inject.Provider

class EnabledBindingResolverFactory @Inject constructor(
    private val enabledBindingResolverInteractorProvider: Provider<EnabledBindingResolverInteractor>,
) {
    fun create(): EnabledBindingResolverInteractor {
        return enabledBindingResolverInteractorProvider.get()
    }
}
