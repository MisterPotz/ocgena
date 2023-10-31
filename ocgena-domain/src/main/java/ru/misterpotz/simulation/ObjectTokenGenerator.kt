package simulation

import dsl.PatternIdCreator
import ru.misterpotz.marking.objects.EmptyObjectValuesMap
import ru.misterpotz.marking.objects.ObjectToken
import model.ObjectType
import model.PlaceTyping
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.simulation.marking.PlainMarking

class ObjectTokenGenerator() {
    private val generators: MutableMap<ObjectType, SingleTypeObjectTokenGenerator> = mutableMapOf()

    fun generate(type: ObjectType): ObjectToken {
        val generator = generators.getOrPut(type) {
            SingleTypeObjectTokenGenerator(type)
        }
        return generator.generate()
    }
}

class SingleTypeObjectTokenGenerator(
    val type: ObjectType,
    private val idIssuer: PatternIdCreator = PatternIdCreator(startIndex = 1) {
        "${type.label.first()}$it"
    }
) {

    fun generate(): ObjectToken {
        return ObjectToken(
            id = idIssuer.newIntId(),
            name = idIssuer.lastLabelId,
            type = type,
            ovmap = EmptyObjectValuesMap(),
        )
    }
}

class ObjectMarkingFromPlainCreator(
    private val plainMarking: PlainMarking,
    private val placeTyping: PlaceTyping,
    private val generator: ObjectTokenGenerator,
) {

    fun create(): ImmutableObjectMarking {
        return ImmutableObjectMarking(
            buildMap {
                for (place in plainMarking.allPlaces()) {
                    put(place, buildSet<ObjectToken> {
                        val tokens = plainMarking[place]
                        repeat(tokens) {
                            add(generator.generate(placeTyping[place]))
                        }
                    }.toMutableSet())
                }
            }.toMutableMap()
        )
    }
}
