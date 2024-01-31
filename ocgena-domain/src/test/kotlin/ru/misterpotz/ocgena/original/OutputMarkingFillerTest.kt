package ru.misterpotz.ocgena.original

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.ocgena.buildSimplestOCNetNoVar
import ru.misterpotz.ocgena.simComponentOld
import ru.misterpotz.ocgena.defaultSimConfigOriginal
import ru.misterpotz.ocgena.simTask
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec

class OutputMarkingFillerTest {
    @Test
    fun enabledBindingsAreFound() {
        val ocNet = buildSimplestOCNetNoVar()

        val config = defaultSimConfigOriginal(ocNet).copy(
            initialMarking = MarkingScheme.of {
                put("p1", 10)
            },
            transitionsSpec = TransitionsOriginalSpec()
        )

        val component = simComponentOld(config)
        val simTask = simTask(component)
        simTask.prepareRun()

        val enabledBindingResolverInteractor = component.enabledBindingsResolver()
        val transition = component.ocNet().petriAtomRegistry.getTransition("t1")
        val enabledBinding = enabledBindingResolverInteractor.tryGetEnabledBinding(transition)

        Assertions.assertNotNull(enabledBinding, "enabled binding resolver seems to be off")
    }
}