package ru.misterpotz.ocgena.simulation.queue

import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.marking.objects.ObjectToken
import ru.misterpotz.marking.objects.ObjectTokenSet
import ru.misterpotz.simulation.config.SimulationConfig
import javax.inject.Inject

class TokenGenerationFacade @Inject constructor(
    private val objectTokenSet: ObjectTokenSet,
    private val simulaionConfig: SimulationConfig
) {
    private val objectTokenGenerator = simulaionConfig.objectTokenGenerator
    fun generate(type: ObjectType): ObjectToken {
        val generated = objectTokenGenerator.generate(type)
        objectTokenSet.add(generated)
        return generated
    }
}
