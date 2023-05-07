import config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.js.jso
import model.OcNetType
import simulation.client.Client
import simulation.client.OnReadinessCallback
import simulation.client.SimCallback
import simulation.config.SimulationConfig
import utils.mprintln
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClientTest {
    fun createOcDot() : String {
        return """ocnet {
            |   places { 
            |       p1 p2 p3
            |   }
            |   transitions {
            |       t1 t2
            |   }
            |   p1 -> t1 -> p2 -> t2 -> p3
            |}
        """.trimMargin()
    }
    fun createSimConfig(): SimulationConfig {
        return createConfig(
            inputPlacesConfig = InputPlacesConfig("p1"),
            outputPlacesConfig = OutputPlacesConfig("p3"),
            ocNetTypeConfig =  OCNetTypeConfig.from(OcNetType.TYPE_A),
            labelMappingConfig = LabelMappingConfig(
                jso {
                    t1 = "Initialization"
                    t2 = "Execution"
                }
            ),
            initialMarkingConfig = InitialMarkingConfig(
                jso {
                    p1 = 4
                }
            ),
            // place type config
            // transition config
        )
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun prepare() {
        val scheduler = TestCoroutineScheduler()
        mprintln("setting test scheduler ${scheduler.hashCode()}")
        val mainDispatcher = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(mainDispatcher)
    }

    @Test
    fun simpleModelBuildingClientTest() = runTest {
        val simulationConfig = createSimConfig()
        val ocDot = createOcDot()

        var successObserved = false
        val client = Client(
            object : OnReadinessCallback {
                override fun readyToCalc(boolean: Boolean) {
                    mprintln("readiness callback invoked $boolean")
                    successObserved = boolean
                }
            }
        )
        client.start()
        client.updateOcDot(ocDot)
        client.updateConfig(simulationConfig)

        mprintln("test scheduler in test ${testScheduler.hashCode()}")

        advanceUntilIdle()

        assertTrue(successObserved, "on ready callback either not invoked or critical error detected in the model")
    }

    @Test
    fun simpleModelSimulationClientTest() = runTest {
        val simulationConfig = createSimConfig()
        val ocDot = createOcDot()

        var successObserved = false
        val client = Client(
            object : OnReadinessCallback {
                override fun readyToCalc(boolean: Boolean) {
                    mprintln("readiness callback invoked $boolean")
                    successObserved = boolean
                }
            }
        )
        client.start()
        client.updateOcDot(ocDot)
        client.updateConfig(simulationConfig)

        mprintln("test scheduler in test ${testScheduler.hashCode()}")

        advanceUntilIdle()

        assertTrue(successObserved, "on ready callback either not invoked or critical error detected in the model")

        val simTask = client.createClientSimTask()
        assertNotNull(simTask, "expected successfully created simulation task")

        var invoked = false
        simTask.launch(
            object : SimCallback {
                override fun onFinishedSimulation() {
                    invoked = true
                }
            }
        )
        advanceUntilIdle()
        assertTrue(invoked)
    }
}
