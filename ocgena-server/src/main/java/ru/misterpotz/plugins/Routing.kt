package ru.misterpotz.plugins

import io.ktor.http.cio.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.misterpotz.startSimulation

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/start") {
            startSimulation()
        }

        get("/ocel/{name?}") {
            val fixed = if (call.parameters["name"].isNullOrEmpty().not()) {
                val path = call.parameters["name"]!!
                val fixed = if (path.endsWith(".db")) {
                    "$path.db"
                } else {
                    path
                }
                fixed
            } else {
                "data.db"
            }

            println("getting at $fixed")

            call.respond("getting at $fixed")
        }
    }
}
