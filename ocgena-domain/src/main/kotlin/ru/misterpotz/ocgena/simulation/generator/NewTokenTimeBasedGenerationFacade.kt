package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.collections.objects.ObjectTokenSet
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import javax.inject.Inject

class NewTokenTimeBasedGenerationFacade @Inject constructor(
    private val objectTokenSet: ObjectTokenSet,
    simulationConfig: SimulationConfig
) {
    private val objectTokenGenerator = simulationConfig.objectTokenGenerator
    fun generate(type: ObjectType): ObjectToken {
        val generated = objectTokenGenerator.generate(type)
        objectTokenSet.add(generated)
        return generated
    }
}
