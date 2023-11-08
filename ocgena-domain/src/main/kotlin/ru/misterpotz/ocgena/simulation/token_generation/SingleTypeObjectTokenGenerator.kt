package ru.misterpotz.ocgena.simulation.token_generation

import ru.misterpotz.ocgena.simulation.EmptyObjectValuesMap
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.utils.PatternIdCreator


class SingleTypeObjectTokenGenerator(
    val type: ObjectType,
    private val idIssuer: PatternIdCreator = PatternIdCreator() {
        "${type.id.first()}$it"
    }
) {

    fun generate(): ObjectToken {
        return ObjectToken(
            id = idIssuer.newIntId,
            name = idIssuer.lastLabelId,
            type = type,
            ovmap = EmptyObjectValuesMap(),
        )
    }
}
