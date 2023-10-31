package ru.misterpotz.simulation.queue

import model.ObjectType
import ru.misterpotz.marking.objects.ObjectToken
import ru.misterpotz.marking.objects.ObjectTokenSet
import ru.misterpotz.simulation.config.SimulationConfig
import javax.inject.Inject

class TokenGenerationFacade @Inject constructor(
    val objectTokenSet: ObjectTokenSet,
    val simulaionConfig: SimulationConfig
) {
    private val objectTokenGenerator = simulaionConfig.objectTokenGenerator
    fun generate(type: ObjectType): ObjectToken {
        val generated = objectTokenGenerator.generate(type)
        objectTokenSet[generated.id]
    }
}