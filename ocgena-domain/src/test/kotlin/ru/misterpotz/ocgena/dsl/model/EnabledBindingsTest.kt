package ru.misterpotz.ocgena.dsl.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.dsl.buildSimplestOCNetNoVar
import ru.misterpotz.ocgena.dsl.simComponent
import ru.misterpotz.ocgena.dsl.defaultSimConfig
import ru.misterpotz.ocgena.dsl.simTask
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.TransitionInstancesTimesSpec

class EnabledBindingsTest {
    @Test
    fun enabledBindingsAreFound() {
        val ocNet = buildSimplestOCNetNoVar()

        val config = defaultSimConfig(ocNet).copy(
            initialMarking = MarkingScheme.of {
                put("p1", 10)
            },
            transitionInstancesTimesSpec = TransitionInstancesTimesSpec(),
        )

        val component = simComponent(config)
        val simTask = simTask(component)
        simTask.prepareRun()

        val enabledBindingResolverInteractor = component.enabledBindingsResolver()
        val transition = component.ocNet().petriAtomRegistry.getTransition("t1")
        val enabledBinding = enabledBindingResolverInteractor.tryGetEnabledBinding(transition)

        Assertions.assertNotNull(enabledBinding, "enabled binding resolver seems to be off")
    }
}