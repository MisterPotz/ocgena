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
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponent
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.testing.buildConfig
import ru.misterpotz.ocgena.testing.buildOCNet
import ru.misterpotz.ocgena.testing.buildingBlockTwoInTwoOutMiddle
import ru.misterpotz.ocgena.testing.installOnto
import kotlin.io.path.Path

object ServiceProvider {
    val domainComponent by lazy {
        DomainComponent.create()
    }

    val serverComponent by lazy {
        ServerComponent.create(domainComponent)
    }
}

//suspend fun convertToOcel() {
//    val simulationToLogConversionParams = SimulationToLogConversionParams(
//        simulationLogDBPath = Path("data", "data.db"),
//        ocelDBPath = Path("ocel", "convert.db"),
//        ocNetStruct = TODO("need to provide ocnetstruct at least to infer db structure of the log")
//    )
//    val simulationToLogConversion = SimulationToLogConversionComponent.create(
//        simulationToLogConversionParams
//    )
//
//    val converter = simulationToLogConversion.converter()
//    converter.convert()
//}

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
