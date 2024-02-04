package ru.misterpotz.ocgena.timepn

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.testing.*

class CompleteRunTest {
    @Test
    fun `complete run test`() = runTest {
        val config = buildConfig {
            this.ocNetStruct = buildOCNet {
                buildingBlockTwoInTwoOutMiddle().installOnto(this)
            }
            this.ocNetType = OcNetType.LOMAZOVA
            this.semanticsType = SimulationSemanticsType.SIMPLE_TIME_PN
        }.withInitialMarking {
            put("p1", 2)
        }
        val simComp = config.toSimComponent()
        simComp.simulationTask().prepareAndRunAll()
    }
}