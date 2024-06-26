package ru.misterpotz.di

import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.*
import kotlinx.serialization.modules.SerializersModule
import ru.misterpotz.ocgena.di.DomainComponent
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@Module
abstract class ServerModule {
    companion object {

        @Provides
        @ServerScope
        fun provideSimulationJobDispatcher(): CoroutineDispatcher {
            return Dispatchers.Default
        }

        @Provides
        @ServerScope
        fun jobCoroutineScope(): CoroutineScope {
            return object : CoroutineScope {
                override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
                    throwable.printStackTrace()
                }
            }
        }

        @Provides
        @ServerScope
        fun provideTaskRegistry(coroutineScope: CoroutineScope) : TasksRegistry {
            return TasksRegistry(coroutineScope)
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