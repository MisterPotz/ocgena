package ru.misterpotz

import ru.misterpotz.plugins.configureMonitoring
import ru.misterpotz.plugins.configureRouting
import ru.misterpotz.plugins.configureSerialization
import ru.misterpotz.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
