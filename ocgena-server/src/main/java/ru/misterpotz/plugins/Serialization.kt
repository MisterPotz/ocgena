package ru.misterpotz.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import ru.misterpotz.ServiceProvider

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                classDiscriminator = "type"
                prettyPrint = true
                encodeDefaults = false
                isLenient = true
                ignoreUnknownKeys = true

                serializersModule = ServiceProvider.serverComponent.serializersModule()
            }
        )
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
