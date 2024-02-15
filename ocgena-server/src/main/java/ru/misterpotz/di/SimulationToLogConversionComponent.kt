package ru.misterpotz.di

import com.zaxxer.hikari.HikariDataSource
import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.jetbrains.exposed.sql.Database
import ru.misterpotz.OCNetToOCELConverter
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
        @IntoMap
        @StringKey(SIM_DB)
        fun provideSimulationLogDBConnection(
            simulationToLogConversionParams: SimulationToLogConversionParams,
            dbConnectionSetupper: DBConnectionSetupper
        ): DBConnectionSetupper.Connection {
            return dbConnectionSetupper.createConnection(simulationToLogConversionParams.simulationLogDBPath)
        }

        @Provides
        @SimulationToLogConversionScope
        @IntoMap
        @StringKey(OCEL_DB)
        fun provideOcelDBConnection(
            simulationToLogConversionParams: SimulationToLogConversionParams,
            dbConnectionSetupper: DBConnectionSetupper
        ): DBConnectionSetupper.Connection {
            return dbConnectionSetupper.createConnection(simulationToLogConversionParams.ocelDBPath)
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
    fun converter() : OCNetToOCELConverter

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance simulationToLogConversionParams: SimulationToLogConversionParams
        ): SimulationToLogConversionComponent
    }

    companion object {
        fun create(
            simulationToLogConversionParams: SimulationToLogConversionParams
        ): SimulationToLogConversionComponent {
            return DaggerSimulationToLogConversionComponent.factory()
                .create(simulationToLogConversionParams)
        }
    }
}

@Scope
annotation class SimulationToLogConversionScope