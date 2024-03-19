package ru.misterpotz.plugins

import io.ktor.http.cio.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.misterpotz.convertToOcel
import ru.misterpotz.startSimulation
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/clean") {
            val path = Path("data","data.db")
            path.deleteIfExists()
        }
        get("/simulate") {
            startSimulation()
        }
        get("/ocel") {
            val path =  Path("data", "data.db")
            if (path.notExists()) {
                call.respond("simulation log does not exist")
            }

            convertToOcel()
            call.respond("getting at $")
        }
    }
}
