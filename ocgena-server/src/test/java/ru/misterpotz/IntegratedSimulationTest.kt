package ru.misterpotz

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.input.Interval
import ru.misterpotz.ocgena.simulation_v2.input.PlaceSetting
import ru.misterpotz.ocgena.simulation_v2.input.SimulationInput
import ru.misterpotz.ocgena.simulation_v2.input.TransitionSetting
import ru.misterpotz.ocgena.testing.build3Tran4InpExample
import ru.misterpotz.plugins.*
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegratedSimulationTest {

    @Test
    fun `serialization works`() {
        val path = Path("testing_output", "integration.db")

        val model = build3Tran4InpExample()
        val simulationinput = SimulationInput(
            places = mapOf(
                "input1" to PlaceSetting(initialTokens = 3),
                "input2" to PlaceSetting(initialTokens = 3),
                "input3" to PlaceSetting(initialTokens = 3),
                "input4" to PlaceSetting(initialTokens = 3)
            ),
            transitions = mapOf(
                "t1" to TransitionSetting(
                    eftLft = Interval(50..100)
                ),
            ),
            randomSeed = 42,
            loggingEnabled = true
        )
        val request = SimulateRequest(outputDatabasePath = path.toString(), simInput = simulationinput, model = model)

        Json { serializersModule = ServiceProvider.serverComponent.serializersModule() }.encodeToString(
            request
        ).let {
            println(it)
            assertTrue { it.isNotEmpty() }
        }
    }

    val path = Path("testing_output", "integration.sqlite")
    val ocelPath = Path("testing_output", "ocel_gen_integration.sqlite")
    val simulationRequest = run {
        val model = build3Tran4InpExample()
        val simulationinput = SimulationInput(
            places = mapOf(
                "input1" to PlaceSetting(initialTokens = 3),
                "input2" to PlaceSetting(initialTokens = 3),
                "input3" to PlaceSetting(initialTokens = 3),
                "input4" to PlaceSetting(initialTokens = 3)
            ),
            transitions = mapOf(
                "t1" to TransitionSetting(
                    eftLft = Interval(50..100)
                ),
            ),
            randomSeed = 42,
            loggingEnabled = true
        )
        SimulateRequest(outputDatabasePath = path.toString(), simInput = simulationinput, model = model)
    }

    @Test
    fun `integrated simulation run`() = testApplication {
        application {
            configureRouting()
            configureSerialization()
            configureMonitoring()
        }
        val client = createClient {
            install(ContentNegotiation) {
                json(ServiceProvider.serverComponent.json)
            }
        }
        path.deleteIfExists()
        val post = client.post("start_simulate") {
            contentType(ContentType.Application.Json)
            setBody(simulationRequest)
        }

        assertEquals(post.status, HttpStatusCode.OK)
        val handle = post.body<SimulateResponse>()

        while (client.get("simulate_res/${handle.handle}").body<ResultResponse>().isInProgress) {
            delay(200)
        }

        assertTrue(path.exists())
    }

    @Test
    fun `integrated simulation and ocel generation run`() {
        testApplication {
            application {
                configureRouting()
                configureSerialization()
                configureMonitoring()
            }
            val client = createClient {
                install(ContentNegotiation) {
                    json(ServiceProvider.serverComponent.json)
                }
            }
            path.deleteIfExists()
            val post = client.post("start_simulate") {
                contentType(ContentType.Application.Json)
                setBody(simulationRequest)
            }
            assertEquals(post.status, HttpStatusCode.OK)
            val handle = post.body<SimulateResponse>()
            while (client.get("simulate_res/${handle.handle}").body<ResultResponse>().isInProgress) {
                delay(200)
            }
            assertTrue(path.exists())
            assertTrue(client.get("simulate_res/${handle.handle}").body<ResultResponse>().isSuccess)

            ocelPath.deleteIfExists()
            val ocelGeneration = client.post("make_ocel") {
                contentType(ContentType.Application.Json)
                setBody(
                    MakeOcelRequest(
                        path.toString(),
                        ocelPath.toString()
                    )
                )
            }
            val ocelHandle = ocelGeneration.body<SimulateResponse>().handle

            while (client.get("ocel_res/${ocelHandle}").body<ResultResponse>().isInProgress) {
                delay(200)
            }
            assertTrue(client.get("ocel_res/${ocelHandle}").body<ResultResponse>().isSuccess)
            assertTrue(ocelPath.exists())
        }
    }
}