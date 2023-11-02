package ru.misterpotz.ocgena.simulation.token_generation

import dsl.PatternIdCreator
import ru.misterpotz.ocgena.simulation.EmptyObjectValuesMap
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectType

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