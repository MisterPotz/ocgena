package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.collections.objects.ObjectTokenSet
import ru.misterpotz.ocgena.collections.objects.ObjectTokenSetMap
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.token_generation.ObjectTokenGenerator
import javax.inject.Inject

class NewTokenTimeBasedGenerationFacade @Inject constructor(
    private val objectTokenSet: ObjectTokenSet,
    private val objectTokenGenerator: ObjectTokenGenerator
) {
    fun generate(type: ObjectType): ObjectToken {
        val generated = objectTokenGenerator.generate(type)
        objectTokenSet.add(generated)
        return generated
    }
}
