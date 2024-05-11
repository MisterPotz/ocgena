package ru.misterpotz.ocgena.simulation_v2.algorithm.simulation

import ru.misterpotz.ocgena.simulation_v2.entities_storage.SimpleTokenSlice
import ru.misterpotz.ocgena.simulation_v2.entities_storage.TokenSlice
import java.lang.IllegalStateException
import java.util.SortedMap
import java.util.SortedSet


class TransitionArcSolver(
    val transition: TransitionWrapper,
) {

    fun getSolutions(tokenSlice: TokenSlice) {
        // need to consider the conditions
        // filtering it is

        val allPlacesHaveEnoughTokensForCondition = transition.inputArcs.all { arc ->
            arc.consumptionSpec.complies(tokenSlice.amountAt(arc.fromPlace))
        }

        if (!allPlacesHaveEnoughTokensForCondition) {
            return
        }
        // проверили что токенов в принципе хватит, дальше че?
        // нужно начать с самого забористого условия
        // как определить самое забористое условие? где пересекаются несколько транзишенов


        val strongestArcsApplicableTokens =
            transition.inputArcConditions.associateBy(
                keySelector = { it.arcWithStrongestCondition },
                valueTransform = { inputCondition ->
                    inputCondition.arcWithStrongestCondition.selectApplicableTokens(tokenSlice)
                    inputCondition.arcWithStrongestCondition.currentSolutionSeachFilteredTokens!!
                }
            )

        val strongestArcsHaveEnoughSatisfactoryTokens = strongestArcsApplicableTokens.all { (arc, tokens) ->
            arc.consumptionSpec.complies(tokens.size)
        }

        if (!strongestArcsHaveEnoughSatisfactoryTokens) {
            return
        }

        val buffer = SimpleTokenSlice(tokenSlice.relatedPlaces.toMutableSet())
        strongestArcsApplicableTokens.forEach { (arc, tokens) ->
            {
                buffer.modifyTokensAt(arc.fromPlace) {
                    it.addAll(tokens)
                }
            }

            // теперь нужно найти для каждого группового условия подходящие по синхронизации варианты
            // надо как-то не очень прямолинейно число солюшенов искать, надо за это число принять другое, солюшенов
            // может быть гипер дохрена и больше и нормально это может не получиться посчитать


            val inputArc = transition.inputArcs.maxBy { it.syncTransitions.size }
            if (inputArc.syncTransitions.size == 0) {
                // do standard logic
            } else {
                // applicable tokens
                val tokens = tokenSlice.tokensAt(inputArc.fromPlace).filter { token ->
                    inputArc.syncTransitions.all { it in token.visitedTransitions }
                }
            }

            transition.inputArcConditions.map {

            }


            // need to start from the most narrow condition

//            for ()
        }
    }

    class ArcGroupCondition(
        val transition: TransitionWrapper,
        val fromPlaces: Places,

        ) {
        fun isSatisfied(tokenSlice: TokenSlice) {

        }

//        fun get
    }
}