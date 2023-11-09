package ru.misterpotz.ocgena.simulation_old.generator

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenSet
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.simulation_old.ObjectToken
import ru.misterpotz.ocgena.simulation_old.structure.SimulatableOcNetInstance
import ru.misterpotz.ocgena.simulation_old.token_generation.ObjectTokenGenerator
import javax.inject.Inject

class NewTokenGenerationFacade @Inject constructor(
    private val objectTokenSet: ObjectTokenSet,
    private val simulatableOcNetInstance: SimulatableOcNetInstance,
    private val objectTokenGenerator: ObjectTokenGenerator
) {

    fun generateRealToken(typeId: ObjectTypeId): ObjectToken {
        val type = simulatableOcNetInstance.objectTypeRegistry[typeId]

        val generated = objectTokenGenerator.generate(type)
        objectTokenSet.add(generated)
        return generated
    }
}
