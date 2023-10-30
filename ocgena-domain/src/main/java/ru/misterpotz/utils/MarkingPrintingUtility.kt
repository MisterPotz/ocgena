package ru.misterpotz.utils

import model.*
import ru.misterpotz.model.*
import ru.misterpotz.model.ImmutableObjectMarkingMap
import ru.misterpotz.model.ObjectMarkingMap
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

    fun prettyPrint(objectMarking: ObjectMarking): String {
        return (objectMarking as ObjectMarkingMap).placesToObjectTokens.let { prettyPrint(it) }
    }

    fun prettyPrint(objectMarking: ImmutableObjectMarking): String {
        return (objectMarking as ImmutableObjectMarkingMap).placesToObjectTokens.let { prettyPrint(it) }
    }

    private var stringBuilder = StringBuilder()

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


    fun toString(objectMarking: ObjectMarking): String {
        return (objectMarking as ObjectMarkingMap).placesToObjectTokens.let { prettyPrint(it) }
    }

    fun toString(objectMarking: ImmutableObjectMarking): String {
        return (objectMarking as ImmutableObjectMarkingMap).placesToObjectTokens.let { prettyPrint(it) }
    }
}