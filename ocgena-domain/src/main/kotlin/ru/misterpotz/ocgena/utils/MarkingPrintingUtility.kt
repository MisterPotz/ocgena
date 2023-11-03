package ru.misterpotz.ocgena.utils

import model.*
import ru.misterpotz.ocgena.collections.objects.*
import ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import utils.print
import javax.inject.Inject

class MarkingPrintingUtility @Inject constructor(private val objectTokenSet: ObjectTokenSet) {
    fun <S : Set<ObjectTokenId>> prettyPrint(placesToObjectTokens: Map<PlaceId, S>): String {
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

    fun prettyPrint(objectMarking: ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking): String {
        return prettyPrint((objectMarking as ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarkingMap).placesToObjectTokens)
    }

    fun <S : Set<ObjectTokenId>> toString(placesToObjectTokens: Map<PlaceId, S>): String {
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

    fun toString(objectMarking: ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking): String {
        return toString((objectMarking as ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarkingMap).placesToObjectTokens)
    }
}