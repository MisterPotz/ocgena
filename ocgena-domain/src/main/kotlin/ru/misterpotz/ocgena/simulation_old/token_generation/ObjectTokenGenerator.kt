package ru.misterpotz.ocgena.simulation_old.token_generation

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenSet
import ru.misterpotz.ocgena.simulation_old.ObjectToken
import ru.misterpotz.ocgena.simulation_old.ObjectTokenId
import ru.misterpotz.ocgena.simulation_old.ObjectType
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