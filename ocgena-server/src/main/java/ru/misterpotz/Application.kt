package ru.misterpotz

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.misterpotz.di.ServerComponent
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.plugins.configureMonitoring
import ru.misterpotz.plugins.configureRouting
import ru.misterpotz.plugins.configureSerialization
import ru.misterpotz.plugins.configureSockets

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
