package ru.misterpotz

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.simulation_v2.input.*
import ru.misterpotz.ocgena.testing.buildSynchronizingLomazovaExampleModel
import ru.misterpotz.plugins.*
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegratedSynchronizationSimulation {
    val ocnet = buildSynchronizingLomazovaExampleModel()
    val simulationInput = SimulationInput(
        randomSeed = 42,
        loggingEnabled = true,
        defaultEftLft = Interval(10..120),
        transitions = mapOf(
            "test_all_sync" to TransitionSetting(
                synchronizedArcGroups = listOf(
                    SynchronizedArcGroup(syncTransition = "send_invoices", listOf("b2", "o3")),
                    SynchronizedArcGroup("place_order", listOf("o3", "p3")),
                    SynchronizedArcGroup("arrange_packages_to_tracks", listOf("p3", "t2"))
                )
            )
        ),
        places = mapOf(
            "bill" to PlaceSetting(initialTokens = 4),
            "package" to PlaceSetting(initialTokens = 12),
            "order" to PlaceSetting(initialTokens = 4),
            "track" to PlaceSetting(initialTokens = 4)
        )
    )
    val path = Path("testing_output", "sync_integration.sqlite")
    val ocelPath = Path("testing_output", "sync_ocel_gen_integration.sqlite")
    val simulationRequest = SimulateRequest(outputDatabasePath = path.toString(), simulationInput, ocnet)

    @Test
    fun `integrated synchronization simulation and ocel generation test`() = testApplication {
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
        assertEquals(null, client.get("simulate_res/${handle.handle}").body<ResultResponse>().message)

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