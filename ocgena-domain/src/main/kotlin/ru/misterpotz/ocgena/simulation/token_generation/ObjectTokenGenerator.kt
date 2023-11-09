package ru.misterpotz.ocgena.simulation.token_generation

import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectType

class ObjectTokenGenerator {
    private val generators: MutableMap<ObjectType, SingleTypeObjectTokenGenerator> = mutableMapOf()

    fun generate(type: ObjectType): ObjectToken {
        val generator = generators.getOrPut(type) {
            SingleTypeObjectTokenGenerator(type)
        }
        return generator.generate()
    }
}