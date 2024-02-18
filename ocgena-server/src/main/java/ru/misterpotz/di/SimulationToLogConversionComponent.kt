package ru.misterpotz.di

import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import ru.misterpotz.InAndOutPlacesColumnProducer
import ru.misterpotz.TokenSerializer
import ru.misterpotz.convert.OCNetToOCELConverter
import ru.misterpotz.convert.OcelTablesProvider
import ru.misterpotz.convert.OcelTablesProviderImpl
import ru.misterpotz.convert.SimulationLogReadRepositoryImpl
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.db.DBConnectionSetupperImpl
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.simulation.*
import ru.misterpotz.simulation.TablesProviderImpl
import java.nio.file.Path
import javax.inject.Scope

@Module
internal abstract class SimulationToLogConversionModule {
    @Binds
    @SimulationToLogConversionScope
    abstract fun bindTablesProvider(tablesProviderImpl: TablesProviderImpl): TablesProvider

    @Binds
    @SimulationToLogConversionScope
    abstract fun bindOcelTablesProvider(ocelTablesProviderImpl: OcelTablesProviderImpl): OcelTablesProvider

    @Binds
    @SimulationToLogConversionScope
    abstract fun bindDBConnectionSetupper(dbConnectionSetupper: DBConnectionSetupperImpl): DBConnectionSetupper

    companion object {
        @Provides
        @SimulationToLogConversionScope
        fun provideSimulationLogReadRepository(
            map: Map<String, DBConnectionSetupper.Connection>,
            tablesProviderImpl: TablesProvider,
            inAndOutPlacesColumnProducer: InAndOutPlacesColumnProducer,
            tokenSerializer: TokenSerializer
        ): SimulationLogReadRepository {
            return SimulationLogReadRepositoryImpl(
                db = map.getSimDB().database,
                tablesProvider = tablesProviderImpl,
                inAndOutPlacesColumnProducer = inAndOutPlacesColumnProducer,
                tokenSerializer = tokenSerializer,
            )
        }

        @Provides
        @SimulationToLogConversionScope
        fun provideOCNetStruct(simulationToLogConversionParams: SimulationToLogConversionParams): OCNetStruct {
            return simulationToLogConversionParams.ocNetStruct
        }

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
    val ocelDBPath: Path,
    val ocNetStruct: OCNetStruct
)

@SimulationToLogConversionScope
@Component(modules = [SimulationToLogConversionModule::class])
internal interface SimulationToLogConversionComponent {
    fun converter(): OCNetToOCELConverter

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