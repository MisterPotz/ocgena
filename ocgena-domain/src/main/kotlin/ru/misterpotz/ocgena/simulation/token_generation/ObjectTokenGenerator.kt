package ru.misterpotz.ocgena.simulation.token_generation

import ru.misterpotz.ocgena.collections.ObjectTokenSet
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation.di.SimulationScope
import javax.inject.Inject

class ObjectTokenGenerator @Inject constructor(
    private val objectTokenSet: ObjectTokenSet
) {
    private val generators: MutableMap<ObjectType, SingleTypeObjectTokenGenerator> = mutableMapOf()

    fun generate(type: ObjectType, id : ObjectTokenId? = null): ObjectToken {
        val generator = generators.getOrPut(type) {
            SingleTypeObjectTokenGenerator(type, objectTokenSet)
        }
        return generator.generate(id)
    }
}