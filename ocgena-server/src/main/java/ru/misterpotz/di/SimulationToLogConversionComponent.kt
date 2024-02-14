package ru.misterpotz.di

import com.zaxxer.hikari.HikariDataSource
import dagger.*
import dagger.multibindings.IntoMap
import org.jetbrains.exposed.sql.Database
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.db.SimulationLogRepository
import ru.misterpotz.db.SimulationLogRepositoryImpl
import java.nio.file.Path
import javax.inject.Qualifier
import javax.inject.Scope

@Module
abstract class SimulationToLogConversionModule {
    @Binds
    @SimulationToLogConversionScope
    abstract fun bindSimulationLogRepository(
        simulationLogRepositoryImpl: SimulationLogRepositoryImpl
    ): SimulationLogRepository


    companion object {

        @Provides
        @SimulationToLogConversionScope
        @SimulationLogDB
        fun provideSimulationLogDBConnection(
            simulationToLogConversionParams: SimulationToLogConversionParams,
            dbConnectionSetupper: DBConnectionSetupper
        ): DBConnectionSetupper.Connection {
            return dbConnectionSetupper.createConnection(simulationToLogConversionParams.simulationLogDBPath)
        }

        @Provides
        @SimulationToLogConversionScope
        @OcelDB
        @IntoMap

        fun provideOcelDBConnection(
            simulationToLogConversionParams: SimulationToLogConversionParams,
            dbConnectionSetupper: DBConnectionSetupper
        ) : DBConnectionSetupper.Connection {
            return dbConnectionSetupper.createConnection(simulationToLogConversionParams.ocelDBPath)
        }

        @Provides
        @ServerSimulationScope
        fun provideHikariDataSource(connection: DBConnectionSetupper.Connection): HikariDataSource {
            return connection.hikariDataSource
        }

        @Provides
        @ServerSimulationScope
        fun provideDB(connection: DBConnectionSetupper.Connection): Database {
            return connection.database
        }

        @Provides
        @ServerSimulationScope
        fun simulationConfig(serverSimulationConfig: ServerSimulationConfig) = serverSimulationConfig.simulationConfig
    }
}

data class SimulationToLogConversionParams(
    val simulationLogDBPath: Path,
    val ocelDBPath: Path
)

@SimulationToLogConversionScope
@Component(modules = [SimulationToLogConversionModule::class])
interface SimulationToLogConversionComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationToLogConversionParams: SimulationToLogConversionParams
        ): SimulationToLogConversionComponent
    }
}

@Qualifier
annotation class SimulationLogDB

@Qualifier
annotation class OcelDB

@Scope
annotation class SimulationToLogConversionScope