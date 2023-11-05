package ru.misterpotz.ocgena.utils

import ru.misterpotz.ocgena.collections.*
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import javax.inject.Inject

class MarkingPrintingUtility @Inject constructor(private val objectTokenSet: ObjectTokenSet) {
    fun <S : Set<ObjectTokenId>> prettyPrint(placesToObjectTokens: Map<PetriAtomId, S>): String {
        return placesToObjectTokens.entries.fold(StringBuilder()) { accum, line ->
            accum.append(line.key)
            accum.append(" |\n")
            accum.append(line.value.joinToString(separator = "\n") {
                objectTokenSet[it]!!.name
            }.prependIndent(" "))
            accum.append('\n')
            accum
        }.toString()
    }

    fun prettyPrint(objectMarking: PlaceToObjectMarking): String {
        return prettyPrint((objectMarking as PlaceToObjectMarkingMap).placesToObjectTokens)
    }

    fun prettyPrint(objectMarking: ImmutablePlaceToObjectMarking): String {
        return prettyPrint((objectMarking as ImmutablePlaceToObjectMarkingMap).placesToObjectTokens)
    }

    fun <S : Set<ObjectTokenId>> toString(placesToObjectTokens: Map<PetriAtomId, S>): String {
        return placesToObjectTokens.keys.joinToString(separator = " ") { place ->
            val objectTokens = placesToObjectTokens[place]!!

            val objectTokensString =
                objectTokens.joinToString(separator = " ") {
                    val objectToken = objectTokenSet[it]!!
                    "${objectToken.name}[${objectToken.ownPathTime.print()}]"
                }
            """${place}: $objectTokensString"""
        }
    }

    fun toString(objectMarking: PlaceToObjectMarking): String {
        return toString((objectMarking as PlaceToObjectMarkingMap).placesToObjectTokens)
    }

    fun toString(objectMarking: ImmutablePlaceToObjectMarking): String {
        return toString((objectMarking as ImmutablePlaceToObjectMarkingMap).placesToObjectTokens)
    }
}