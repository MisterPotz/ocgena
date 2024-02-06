package ru.misterpotz.di

import com.charleskorn.kaml.Yaml
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dagger.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import ru.misterpotz.*
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.di.SimulationComponentDependencies
import java.nio.file.Path
import java.sql.Connection
import javax.inject.Scope
import kotlin.io.path.pathString

@Module
internal abstract class ServerSimulationModule {

    @Binds
    @ServerSimulationScope
    abstract fun bindDBLogger(dbLogger: DBLoggerImpl): DBLogger

    @Binds
    @ServerSimulationScope
    abstract fun bindTablesProvider(tablesProviderImpl: TablesProviderImpl): TablesProvider

    @Binds
    @ServerSimulationScope
    abstract fun bindSimulationLogRepository(
        simulationLogRepositoryImpl: SimulationLogRepositoryImpl
    ):
            SimulationLogRepository


    companion object {

        @Provides
        @ServerSimulationScope
        fun provideHikariDataSource(serverSimulationConfig: ServerSimulationConfig): HikariDataSource {
            val path = serverSimulationConfig.dbPath
            val hikariSqlConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:${path.pathString}"
                driverClassName = "org.sqlite.JDBC"
            }
            return HikariDataSource(hikariSqlConfig)
        }

        @Provides
        @ServerSimulationScope
        fun provideDB(hikariDataSource: HikariDataSource): Database {
            val connect = Database.connect(hikariDataSource)
            TransactionManager.manager.defaultIsolationLevel =
                Connection.TRANSACTION_SERIALIZABLE

            return connect
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
