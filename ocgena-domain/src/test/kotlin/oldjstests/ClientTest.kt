package oldjstests

import config.*
import error.Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import model.OcDotParseResult
import model.OcNetType
import simulation.client.*
import simulation.config.SimulationConfig
import utils.mprintln
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClientTest {
    fun createOcDot(): String {
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
        return createConfigFast(
            ocNetTypeConfig = OcNetType.AALST,
            initialMarkingConfig = "p1: 3;",
            inputPlaces = "p1",
            outputPlaces = "p3",
            labelMapping = "t1: Initialization; t2: Execution",
            defaultTransitionIntervals = "default: d[1,1] min[2,2]",
            transitionsIntervalsMap = "t1: d[1,1] min[1,2]",
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
                override fun ocDotParseResult(ocDotParseResult: OcDotParseResult) {

                }

                override fun onCurrentErrorsChange(errors: Array<Error>?) {
                }

                override fun readyToCalc(boolean: Boolean) {
                    mprintln("readiness callback invoked $boolean")
                    successObserved = boolean
                }
            }
        )
        client.loggingEnabled = false
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
                override fun ocDotParseResult(ocDotParseResult: OcDotParseResult) {

                }

                override fun onCurrentErrorsChange(errors: Array<Error>?) {
                }

                override fun readyToCalc(boolean: Boolean) {
                    mprintln("readiness callback invoked $boolean")
                    successObserved = boolean
                }
            }
        )
        client.loggingEnabled = true
        client.start()
        client.updateOcDot(ocDot)
        client.updateConfig(simulationConfig)

        mprintln("test scheduler in test ${testScheduler.hashCode()}")

        advanceUntilIdle()

        assertTrue(successObserved, "on ready callback either not invoked or critical error detected in the model")

        val simTaskFactory = client.createClientSimTaskFactory()
        val htmlStringBuilderWriter = HtmlDebugTraceBuilderWriter()
        val ocelWriter = OcelWriter {
        }

        var invoked = false

        val simTask = simTaskFactory!!.create(
            simTaskClientCallback = createSimpleClientCallback(onExecutionFinish = {
                println("finished successfully")
                val html = htmlStringBuilderWriter.collect()
                println(html)
                val ocel = ocelWriter.collect()
                println(JSON.stringify(ocel))
                invoked = true
            }),
            htmlTraceFileWriter = htmlStringBuilderWriter,
            ansiTraceWriter = CallbackStringWriter { _ ->
            },
            ocelWriter = ocelWriter,
            onSimulationStatusUpdate = {
            }
        )

        assertNotNull(simTask, "expected successfully created simulation task")

        while (!simTask.performStep()) {}
        advanceUntilIdle()
        assertTrue(invoked)
    }

    @Test
    fun syncTestLaunch() {
        val simulationConfig = createSimConfig()
        val ocDot = createOcDot()

        var successObserved = false
        val client = Client(
            object : OnReadinessCallback {
                override fun ocDotParseResult(ocDotParseResult: OcDotParseResult) {
                }
                override fun onCurrentErrorsChange(errors: Array<Error>?) {
                }
                override fun readyToCalc(boolean: Boolean) {
                }
            }
        )
//        client.loggingEnabled = false
        client.updateConfig(simulationConfig);
        client.updateOcDot(ocDot)
        val factory = client.createClientSimTaskFactory()!!
        val task = factory.create(
            simTaskClientCallback
            = createSimpleClientCallback(onExecutionFinish = {
//                invoked = true
            }),
            htmlTraceFileWriter = null,
            ansiTraceWriter = CallbackStringWriter { it ->
                   println(it)
            },
            ocelWriter = null,
            onSimulationStatusUpdate = {
            })
        while (!task.performStep()) {

        }
    }
}
