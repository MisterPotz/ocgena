package ru.misterpotz.di

import com.charleskorn.kaml.Yaml
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.serialization.json.Json
import ru.misterpotz.*
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.di.SimulationV2Component
import ru.misterpotz.plugins.SimulateArguments
import ru.misterpotz.simulation.*
import ru.misterpotz.simulation.SimulationLogSinkRepositoryImpl
import javax.inject.Provider
import javax.inject.Scope

@Module
internal abstract class ServerSimulationModule {
    companion object {

        @Provides
        @ServerSimulationScope
        fun provideLogger(
            simulateArguments: SimulateArguments,
            simulationLogRepository: Provider<SimulationLogRepository>,
        ): Logger {
            val dbPath = simulateArguments.simulateRequest.outputDatabasePath
            if (dbPath == null) {
                return NoOpLogger
            } else {
                return DBLoggerImpl(simulationLogRepository.get())
            }
        }

        @Provides
        @ServerSimulationScope
        fun serverSimulationInteractor(
            factory: ServerSimulationInteractor.Factory
        ): ServerSimulationInteractor {
            return factory.created!!
        }

        @Provides
        @ServerSimulationScope
        fun provideDestroyer(
            repository: SimulationLogRepository,
            serverSimulationInteractor: ServerSimulationInteractor
        ): SimulationDestroyer {
            return SimulationDestroyer(repository, serverSimulationInteractor)
        }

        @Provides
        @ServerSimulationScope
        fun provideServerSimulationInteractorFactory(): ServerSimulationInteractor.Factory {
            return ServerSimulationInteractor.Factory()
        }

        @Provides
        @ServerSimulationScope
        fun provideOCNetStruct(simulateArguments: SimulateArguments): OCNetStruct {
            return simulateArguments.simulateRequest.model
        }

        @Provides
        @ServerSimulationScope
        @IntoMap
        @StringKey(SIM_DB)
        fun provideConnection(
            serverSimulationConfig: SimulateArguments,
        ): DBConnectionSetupper.Connection {
            return DBConnectionSetupper.createConnection(serverSimulationConfig.outputPath!!)
        }

        @Provides
        @ServerSimulationScope
        fun provideInAndOutPlaces(
            simulateArguments: SimulateArguments,
        ): InAndOutPlacesColumnProducer {
            return InAndOutPlacesColumnProducer(simulateArguments.simulateRequest.model.placeRegistry.places.map { it.id })
        }

        @Provides
        @ServerSimulationScope
        fun provideTableProvider(
            simulateArguments: SimulateArguments,
        ): TablesProvider {
            return TablesProvider(
                simulateArguments.simulateRequest.model.placeRegistry.places.map { it.id },
            )
        }

        @Provides
        @ServerSimulationScope
        fun provideSimulationLogRepository(
            dbConnections: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards DBConnectionSetupper.Connection>,
            tablesProvider: TablesProvider,
            simulateArguments: SimulateArguments,
            inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
        ): SimulationLogRepository {
            val connection = dbConnections.getSimDB()
            return SimulationLogSinkRepositoryImpl(
                dbConnection = connection,
                tablesProvider = tablesProvider,
                ocNetStruct = simulateArguments.simulateRequest.model,
                inAndOutPlacesColumnProducer = inAndOutPlacesColumnProducer,
                tokenSerializer = TokenSerializer
            )
        }

        @Provides
        @ServerSimulationScope
        fun provideSimulation(
            arguments: SimulateArguments,
            logger: Logger,
            serverSimulationInteractor: ServerSimulationInteractor.Factory
        ): SimulationV2Component {
            return SimulationV2Component.create(
                simulationInput = arguments.simulateRequest.simInput,
                ocNetStruct = arguments.simulateRequest.model,
                simulationV2Interactor = serverSimulationInteractor,
                logger = logger
            )
        }
    }
}

interface ServerSimulationComponentDependencies {
    val json: Json
    val yaml: Yaml
}

class SimulationDestroyer(
    private val repository: SimulationLogRepository,
    private val simulation: ServerSimulationInteractor
) {
    suspend fun destroy() {
        simulation.finish()
        repository.close()
    }
}

@Component(
    modules = [ServerSimulationModule::class],
    dependencies = [ServerSimulationComponentDependencies::class]
)
@ServerSimulationScope
interface ServerSimulationComponent {
    fun simulationV2Component(): SimulationV2Component
    fun destroyer(): SimulationDestroyer

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance arguments: SimulateArguments,
            serverSimulationComponentDependencies: ServerSimulationComponentDependencies
        ): ServerSimulationComponent
    }

    companion object {
        fun create(
            simulateArguments: SimulateArguments,
            serverSimulationComponentDependencies: ServerSimulationComponentDependencies
        ): ServerSimulationComponent {
            return DaggerServerSimulationComponent.factory()
                .create(
                    simulateArguments,
                    serverSimulationComponentDependencies
                )
        }
    }
}

@Scope
annotation class ServerSimulationScope
