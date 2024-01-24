package ru.misterpotz.ocgena.timepn

import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.*
import ru.misterpotz.ocgena.simulation.config.timepn.TransitionsTimePNSpec
import ru.misterpotz.ocgena.simulation.stepexecutor.NewTimeDeltaInteractor
import ru.misterpotz.ocgena.simulation.stepexecutor.TimeShiftSelector

class NewTimeDeltaInteractorTest {

    @Test
    fun `founds correct max time when shift is available`() {
        val simComp = readAndBuildConfig(
            DEFAULT_SETTINGS,
            modelPath = ModelPath.AALST
        ).asTimePNwithSpec(
            TransitionsTimePNSpec()
        ).toSimComponent()


        val newTimeDeltaInteractor = simComp.newTimeDeltaInteractor()

        newTimeDeltaInteractor.generateAndShiftTimeDelta()



    }
}