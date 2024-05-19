package ru.misterpotz.di

import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.modules.SerializersModule
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.db.DBConnectionSetupperImpl
import ru.misterpotz.ocgena.di.DomainComponent
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@Module
abstract class ServerModule {
    @Binds
    @ServerScope
    abstract fun bindDBConnectionSetupper(dbConnectionSetupperImpl: DBConnectionSetupperImpl): DBConnectionSetupper

    companion object {
        @Provides
        @ServerScope
        fun provideTasksRegistry(coroutineScope: CoroutineScope): TasksRegistry {
            return TasksRegistry(scope = coroutineScope)
        }

        @Provides
        @ServerScope
        fun provideSimulationJobDispatcher(): CoroutineDispatcher {
            return Dispatchers.Default
        }

        @Provides
        @ServerScope
        fun jobCoroutineScope(): CoroutineScope {
            return object : CoroutineScope {
                override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()
            }
        }
    }
}

@ServerScope
@Component(
    modules = [ServerModule::class],
    dependencies = [DomainComponent::class]
)
interface ServerComponent : ServerSimulationComponentDependencies {
    fun tasksRegistry(): TasksRegistry
    fun jobDispatcher(): CoroutineDispatcher
    fun jobScope(): CoroutineScope
    fun serializersModule(): SerializersModule

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