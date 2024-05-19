package ru.misterpotz.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.misterpotz.ServiceProvider
import ru.misterpotz.di.ServerSimulationComponent
import ru.misterpotz.di.SimulationToLogConversionComponent
import ru.misterpotz.di.SimulationToLogConversionParams
import ru.misterpotz.di.TasksRegistry
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

enum class RequestProblems(val message: String) {
    MISSING_OUTPUT_LOGGING_PATH("to enable logging, the output database path must be specified")
}

@Serializable
data class SimulateRequest(
    val outputDatabasePath: String?,
    val simInput: SimulationInput,
    val model: OCNetStruct
) {
    fun check(): List<RequestProblems> {
        return buildList {
            if (simInput.loggingEnabled == true && outputDatabasePath == null) {
                add(RequestProblems.MISSING_OUTPUT_LOGGING_PATH)
            }
//            if (outputDatabasePath != null) {
//                val path = Path(outputDatabasePath)
//                if (path.notExists())
//            }
        }
    }

    fun convert(): SimulateArguments {
        return SimulateArguments(outputDatabasePath?.let { Path(it) }, this)
    }
}

@Serializable
data class SimulateResponse(val handle: Long)

suspend fun startSimulation(simulateArguments: SimulateArguments): Long {
    val serverComponent = ServiceProvider.serverComponent

    val serverSimulationComponent =
        ServerSimulationComponent.create(
            simulateArguments,
            serverComponent
        )


    val task = TasksRegistry.Task(
        runCatching {
            serverSimulationComponent.simulationV2Component()
            serverSimulationComponent
        }.map {
            it.simulationV2Component().simulation()
            it
        }.map {
            object : TasksRegistry.Work {
                override suspend fun run() {
                    it.simulationV2Component().simulation().runSimulation()
                }

                override suspend fun destroy() {
                    it.destroyer().destroy()
                }
            }
        }
    )
    return serverComponent.tasksRegistry().launch(task).getOrThrow()
}

suspend fun startOcelGeneration(ocelRequest: SimulationToLogConversionParams): Long {
    val simulationToLogConversionComponent = SimulationToLogConversionComponent.create(ocelRequest)

    val task = TasksRegistry.Task(
        runCatching {
            simulationToLogConversionComponent.converter()
            simulationToLogConversionComponent
        }.map {
            object : TasksRegistry.Work {
                override suspend fun run() {
                    simulationToLogConversionComponent.converter().convert()
                }

                override suspend fun destroy() {
                    //
                }
            }
        }
    )
    return ServiceProvider.serverComponent.tasksRegistry().launch(task).getOrThrow()

}

data class SimulateArguments(
    val outputPath: Path?,
    val simulateRequest: SimulateRequest,
)

@Serializable
data class ResultResponse(
    val isSuccess: Boolean,
    val isError: Boolean,
    val isInProgress: Boolean
)

@Serializable
data class MakeOcelRequest(
    val simulationPath: String,
    val ocelPath: String
) {
    fun toParams(): SimulationToLogConversionParams {
        return SimulationToLogConversionParams(Path(simulationPath), Path(ocelPath))
    }
}

// simulate use case (the path to db, the sim config (serialized), the model (serialized).
// generate ocel use case
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("clean") {
            val path = Path("data", "data.db")
            path.deleteIfExists()
        }
        get("simulate_res/{id}") {
            val handle = call.parameters["id"]!!.toLong()
            val server = ServiceProvider.serverComponent
            val result = server.tasksRegistry().getTaskResult(handle)

            return@get call.respond(
                HttpStatusCode.OK,
                ResultResponse(
                    isSuccess = result?.isOk ?: false,
                    isError = result?.isOk?.not() ?: false,
                    isInProgress = result == null
                )
            )
        }
        get("ocel_res/{id}") {
            val handle = call.parameters["id"]!!.toLong()
            val server = ServiceProvider.serverComponent
            val result = server.tasksRegistry().getTaskResult(handle)

            return@get call.respond(
                HttpStatusCode.OK,
                ResultResponse(
                    isSuccess = result?.isOk ?: false,
                    isError = result?.isOk?.not() ?: false,
                    isInProgress = result == null
                )
            )
        }
        post("start_simulate") {
            val simRequest = call.receive<SimulateRequest>()

            val errors = simRequest.check()
            if (errors.isNotEmpty()) {
                return@post call.respond(HttpStatusCode.BadRequest, message = errors.joinToString(", ") { it.message })
            }
            val arguments = simRequest.convert()

            val handle = runCatching {
                startSimulation(arguments)
            }

            if (handle.isFailure) {
                return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    message = handle.exceptionOrNull()?.message.orEmpty()
                )
            }

            return@post call.respond(HttpStatusCode.OK, message = SimulateResponse(handle = handle.getOrThrow()))
        }
        post("make_ocel") {
            val request = call.receive<MakeOcelRequest>()

            val path = Path(request.simulationPath)
            if (path.notExists()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "simulation log does not exist at given path $path"
                )
            }

            val handle = runCatching {
                startOcelGeneration(request.toParams())
            }
            return@post call.respond(HttpStatusCode.OK, message = SimulateResponse(handle = handle.getOrThrow()))
        }
    }
}
