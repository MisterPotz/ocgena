package ru.misterpotz

import ru.misterpotz.plugins.configureMonitoring
import ru.misterpotz.plugins.configureRouting
import ru.misterpotz.plugins.configureSerialization
import ru.misterpotz.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.misterpotz.di.*
import ru.misterpotz.di.ServerSimulationComponent
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.testing.buildConfig
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.testing.buildingBlockTwoInTwoOutMiddle
import ru.misterpotz.ocgena.testing.installOnto
import kotlin.io.path.Path

fun serverSimulationConfig() = ServerSimulationConfig(
    Path("data", "data.db"),
    simulationConfig = buildConfig {
        ocNetType = OcNetType.LOMAZOVA
        ocNetStruct = buildOCNet {
            buildingBlockTwoInTwoOutMiddle().installOnto(this)
        }
        semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
    }.withInitialMarking {
        put("p1", 10)
    }
)

object ServiceProvider {
    val domainComponent by lazy {
        DomainComponent.create()
    }

    val serverComponent by lazy {
        ServerComponent.create(domainComponent)
    }
}

suspend fun convertToOcel() {
    val simulationToLogConversionParams = SimulationToLogConversionParams(
        simulationLogDBPath = Path("data", "data.db"),
        ocelDBPath = Path("ocel", "convert.db")
    )
    val simulationToLogConversion = SimulationToLogConversionComponent.create(
        simulationToLogConversionParams
    )

    val converter = simulationToLogConversion.converter()
    converter.convert()
}

suspend fun startSimulation() {
    val serverComponent = ServiceProvider.serverComponent

    val serverSimulationComponent =
        ServerSimulationComponent.create(
            serverSimulationConfig(),
            serverComponent
        )

    val simulationComponent =
        SimulationComponent.defaultCreate(
            serverSimulationComponent.simulationConfig(),
            serverSimulationComponent
        )
    simulationComponent.simulationTask().prepareAndRunAll()
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
