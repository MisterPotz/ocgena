package ru.misterpotz.di

import com.charleskorn.kaml.Yaml
import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.serialization.json.Json
import ru.misterpotz.*
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.simulation.SimulationLogRepository
import ru.misterpotz.simulation.SimulationLogSinkRepositoryImpl
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponentDependencies
import ru.misterpotz.simulation.TablesProvider
import ru.misterpotz.simulation.TablesProviderImpl
import java.nio.file.Path
import javax.inject.Scope


@Module
internal abstract class ServerSimulationModule {

    @Binds
    @ServerSimulationScope
    abstract fun bindDBLogger(dbLogger: DBLoggerImpl): Logger

    @Binds
    @ServerSimulationScope
    abstract fun bindTablesProvider(tablesProviderImpl: TablesProviderImpl): TablesProvider

    @Binds
    @ServerSimulationScope
    abstract fun bindSimulationFinishNotifier(
        simulationFinishedNotifierImpl: SimulationFinishedNotifierImpl
    ): SimulationFinishedNotifier

    companion object {

        @Provides
        @ServerSimulationScope
        fun provideOCNetStruct(serverSimulationConfig: ServerSimulationConfig): OCNetStruct {
            return serverSimulationConfig.simulationConfig.ocNet
        }

        @Provides
        @ServerSimulationScope
        @IntoMap
        @StringKey(SIM_DB)
        fun provideConnection(
            serverSimulationConfig: ServerSimulationConfig,
            dbConnectionSetupper: DBConnectionSetupper
        ): DBConnectionSetupper.Connection {
            return dbConnectionSetupper.createConnection(serverSimulationConfig.dbPath)
        }

        @Provides
        @ServerSimulationScope
        fun provideSimulationLogRepository(
            dbConnections: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards DBConnectionSetupper.Connection>,
            tablesProvider: TablesProvider,
            simulationConfig: SimulationConfig,
            inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
            tokenSerializer: TokenSerializer
        ): SimulationLogRepository {
            val connection = dbConnections.getSimDB()
            return SimulationLogSinkRepositoryImpl(
                db = connection.database,
                tablesProvider = tablesProvider,
                simulationConfig = simulationConfig,
                inAndOutPlacesColumnProducer = inAndOutPlacesColumnProducer,
                tokenSerializer = tokenSerializer
            )
        }

        @Provides
        @ServerSimulationScope
        fun simulationConfig(serverSimulationConfig: ServerSimulationConfig) = serverSimulationConfig.simulationConfig
    }
}

data class ServerSimulationConfig(
    val dbPath: Path,
    val simulationConfig: SimulationConfig
)

interface ServerSimulationComponentDependencies {
    val json: Json
    val yaml: Yaml

    fun dbConnectionSetupper(): DBConnectionSetupper
}

@Component(
    modules = [ServerSimulationModule::class],
    dependencies = [ServerSimulationComponentDependencies::class]
)
@ServerSimulationScope
internal interface ServerSimulationComponent : SimulationComponentDependencies {

    fun simulationLogRepository(): SimulationLogRepository
    fun simulationConfig(): SimulationConfig

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance serverSimulationConfig: ServerSimulationConfig,
            serverSimulationComponentDependencies: ServerSimulationComponentDependencies
        ): ServerSimulationComponent
    }

    companion object {
        fun create(
            serverSimulationConfig: ServerSimulationConfig,
            serverSimulationComponentDependencies: ServerSimulationComponentDependencies
        ): ServerSimulationComponent {
            return DaggerServerSimulationComponent.factory()
                .create(
                    serverSimulationConfig,
                    serverSimulationComponentDependencies
                )
        }
    }
}

@Scope
annotation class ServerSimulationScope
