package ru.misterpotz.simulation.token_generation

import model.ObjectType
import ru.misterpotz.marking.objects.ObjectToken

class ObjectTokenGenerator() {
    private val generators: MutableMap<ObjectType, SingleTypeObjectTokenGenerator> = mutableMapOf()

    fun generate(type: ObjectType): ObjectToken {
        val generator = generators.getOrPut(type) {
            SingleTypeObjectTokenGenerator(type)
        }
        return generator.generate()
    }
}