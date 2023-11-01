package ru.misterpotz.simulation.token_generation

import dsl.PatternIdCreator
import model.ObjectType
import ru.misterpotz.marking.objects.EmptyObjectValuesMap
import ru.misterpotz.marking.objects.ObjectToken

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