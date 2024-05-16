package ru.misterpotz.ocgena.simulation_old.token_generation

import ru.misterpotz.ocgena.simulation_old.collections.ObjectTokenSet
import ru.misterpotz.ocgena.ocnet.utils.objPrefix
import ru.misterpotz.ocgena.simulation_old.EmptyObjectValuesMap
import ru.misterpotz.ocgena.simulation_old.ObjectToken
import ru.misterpotz.ocgena.simulation_old.ObjectTokenId
import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.utils.PatternIdCreator


class SingleTypeObjectTokenGenerator(
    val type: ObjectType,
    private val objectTokenSet: ObjectTokenSet,
    private val idIssuer: PatternIdCreator = PatternIdCreator() {
        "$objPrefix$it-${type.id}"
    }
) {

    fun generate(id: ObjectTokenId?): ObjectToken {
        idIssuer.index = id ?: objectTokenSet.biggestId?.let { it + 1 } ?: 0

        return ObjectToken(
            id = idIssuer.newIntId,
            name = idIssuer.lastLabelId,
            objectTypeId = type.id,
            ovmap = EmptyObjectValuesMap(),
        )
    }
}
