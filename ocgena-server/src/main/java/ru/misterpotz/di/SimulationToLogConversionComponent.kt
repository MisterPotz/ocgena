package ru.misterpotz.di

import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.serialization.Serializable
import ru.misterpotz.Logger
import ru.misterpotz.convert.OCNetToOCELConverter
import ru.misterpotz.db.DBConnectionSetupper
import ru.misterpotz.ocgena.simulation_v2.di.SimulationV2Component
import ru.misterpotz.plugins.SimulateArguments
import ru.misterpotz.simulation.*
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Scope
import kotlin.time.DurationUnit

@Module
internal abstract class SimulationToLogConversionModule {

    companion object {
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

        @Provides
        @SimulationToLogConversionScope
        @IntoMap
        @StringKey(SIM_DB)
        fun provideSimulationLogDBConnection(
            simulationToLogConversionParams: SimulationToLogConversionParams
        ): DBConnectionSetupper.Connection {
            return DBConnectionSetupper.createConnection(simulationToLogConversionParams.simulationLogDBPath)
        }

        @Provides
        @SimulationToLogConversionScope
        @IntoMap
        @StringKey(OCEL_DB)
        fun provideOcelDBConnection(
            simulationToLogConversionParams: SimulationToLogConversionParams,
        ): DBConnectionSetupper.Connection {
            return DBConnectionSetupper.createConnection(simulationToLogConversionParams.ocelDBPath)
        }


        @Provides
        @SimulationToLogConversionScope
        fun provideConverter(
            map: Map<String, DBConnectionSetupper.Connection>,
            simulationToLogConversionParams: SimulationToLogConversionParams
        ): OCNetToOCELConverter {
            return OCNetToOCELConverter(
                databases = map,
                conversionParams = simulationToLogConversionParams
            )
        }
    }
}

data class SimulationToLogConversionParams(
    val simulationLogDBPath: Path,
    val ocelDBPath: Path,
    val startingTime: LocalDateTime,
    val unit: DurationUnit
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