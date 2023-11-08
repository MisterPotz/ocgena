package ru.misterpotz.ocgena.simulation.generator

import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation.token_generation.ObjectTokenGenerator
import javax.inject.Inject

class NewTokenTimeBasedGenerationFacade @Inject constructor(
    private val objectTokenSet: ObjectTokenSet,
    private val simulatableOcNetInstance: SimulatableOcNetInstance,
    private val objectTokenGenerator: ObjectTokenGenerator
) {

    fun generate(typeId : ObjectTypeId) : ObjectToken {
        val type = simulatableOcNetInstance.objectTypeRegistry[typeId]

        val generated = objectTokenGenerator.generate(type)
        objectTokenSet.add(generated)
        return generated

    }
}
