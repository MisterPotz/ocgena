package simulation

import dsl.PatternIdCreator
import model.EmptyObjectValuesMap
import model.ObjectMarking
import model.ObjectToken
import model.ObjectType
import model.ObjectTypes
import model.PlaceTyping

class ObjectTokenGenerator(
    private val types: ObjectTypes,
) {
    private val generators: Map<ObjectType, SingleTypeObjectTokenGenerator> = buildMap {
        for (i in types) {
            put(i, SingleTypeObjectTokenGenerator(i))
        }
    }

    fun generate(type: ObjectType): ObjectToken {
        return generators[type]!!.generate()
    }
}

class SingleTypeObjectTokenGenerator(
    val type: ObjectType,
) {
    val idIssuer = PatternIdCreator(startIndex = 1) {
        "${type.label.first()}$it"
    }

    fun generate(): ObjectToken {
        return ObjectToken(
            id = idIssuer.newIntId(),
            name = idIssuer.lastLabelId,
            type = type,
            ovmap = EmptyObjectValuesMap,
        )
    }
}

class ObjectMarkingFromPlainCreator(
    private val plainMarking: PlainMarking,
    private val placeTyping: PlaceTyping,
    private val objectTypes: ObjectTypes,
    private val generator: ObjectTokenGenerator = ObjectTokenGenerator(objectTypes),
) {

    fun create(): ObjectMarking {
        return ObjectMarking(
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
