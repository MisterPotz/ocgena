package ru.misterpotz.di

import com.charleskorn.kaml.Yaml
import dagger.Component
import dagger.Module
import dagger.Subcomponent
import kotlinx.serialization.json.Json
import ru.misterpotz.ocgena.di.DomainComponent
import javax.inject.Singleton

@Module
abstract class ServerModule {
}

@Subcomponent
@Singleton
interface ServerSubcomponent : ServerSimulationComponentDependencies {
    @Singleton
    fun json(): Json

    @Singleton
    fun yaml(): Yaml
}

@Singleton
@Component(
    modules = [ServerModule::class],
    dependencies = [DomainComponent::class]
)
interface ServerComponent : ServerSimulationComponentDependencies {
    fun serverSubcomponent(): ServerSubcomponent

    @Component.Factory
    interface Factory {
        fun create(domainComponent: DomainComponent): ServerComponent
    }

    companion object {
        fun create(domainComponent: DomainComponent): ServerComponent {
            return DaggerServerComponent.create(domainComponent)
        }
    }
}