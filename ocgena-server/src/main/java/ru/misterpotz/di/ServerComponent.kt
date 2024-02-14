package ru.misterpotz.di

import dagger.Binds
import dagger.Component
import dagger.Module
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.db.DBConnectionSetupperImpl
import ru.misterpotz.ocgena.di.DomainComponent
import javax.inject.Scope

@Module
abstract class ServerModule {
    @Binds
    abstract fun bindDBConnectionSetupper(dbConnectionSetupperImpl: DBConnectionSetupperImpl): DBConnectionSetupper
}


@ServerScope
@Component(
    modules = [ServerModule::class],
    dependencies = [DomainComponent::class]
)
interface ServerComponent : ServerSimulationComponentDependencies {

    @Component.Factory
    interface Factory {
        fun create(domainComponent: DomainComponent): ServerComponent
    }

    companion object {
        fun create(domainComponent: DomainComponent): ServerComponent {
            return DaggerServerComponent.factory().create(domainComponent)
        }
    }
}

@Scope
annotation class ServerScope