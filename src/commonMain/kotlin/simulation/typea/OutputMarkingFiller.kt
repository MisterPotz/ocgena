package simulation.typea

import model.ActiveFiringTransition
import model.Arcs
import model.ObjectMarking
import model.ObjectToken
import model.ObjectType
import model.Place
import model.aalst.ArcMultiplicityTypeA

class OutputMarkingFiller(
    private val activeFiringTransition: ActiveFiringTransition,
    private val arcs: Arcs,
    private val arcMultiplicity: ArcMultiplicityTypeA
) {
    val transition = activeFiringTransition.transition
    private val inputPlaces = transition.inputPlaces
    private val outputPlaces = transition.outputPlaces
    private val inputPlacesByType = getTypeToPlaces(inputPlaces)
    private val outputPlacesByType = getTypeToPlaces(outputPlaces)

    private val inputTotalByType = getTokenAmountForTypeToPlaces(inputPlacesByType)
    private val outputTotalByType = getTokenAmountForTypeToPlaces(outputPlacesByType)
    val transitionArcs = arcs[transition]
    val outputMarking = ObjectMarking()
    val inputMarking = activeFiringTransition.lockedObjectTokens

    fun fill() : ObjectMarking {
        checkOtOfInputAndOutput(inputTotalByType.keys, outputTotalByType.keys)
        checkTransferAmountsMatch(inputTotalByType, outputTotalByType)

        return fillOutputMarking()
    }

    private fun fillMarkingVariable(
        otype: ObjectType,
        outputPlace: Place,
    ) {
        val theInputPlace = inputPlacesByType[otype]!!.first()
        val suppliedTokens = inputMarking[theInputPlace]!!
        outputMarking[outputPlace] = mutableSetOf<ObjectToken>().also {
            it.addAll(suppliedTokens)
        }
        suppliedTokens.clear()
    }

    private fun fillMarkingForNormal(
        otype: ObjectType,
        outputPlace : Place,
        transferAmount: ArcMultiplicityTypeA.TransferAmount.Normal,
    ) {
        val requiredAmount = transferAmount.amount
        val consumedTokens = mutableSetOf<ObjectToken>()

        for (inputPlace in inputPlacesByType[otype]!!) {
            val tokensOfInputPlace = inputMarking[inputPlace]!!
            if (tokensOfInputPlace.isEmpty()) continue
            val tokensTakenFromInputPlace = tokensOfInputPlace.toList().let {
                it.slice(0..it.lastIndex.coerceAtMost(requiredAmount))
            }
            tokensOfInputPlace -= tokensTakenFromInputPlace.toSet()

            consumedTokens.addAll(tokensTakenFromInputPlace)

            if (consumedTokens.size == requiredAmount) {
                break
            }
        }
        outputMarking[outputPlace] = consumedTokens
    }

    private fun fillOutputMarking() : ObjectMarking {
        val outputMarking = ObjectMarking()
        val transitionArcs = arcs[transition]

        for (otype in outputPlacesByType.keys) {
            for (outputPlace in outputPlacesByType[otype]!!) {
                val arc = transitionArcs[outputPlace]!!

                when (val transferAmount = arcMultiplicity.getAbstractTransferAmount(arc)) {
                    is ArcMultiplicityTypeA.TransferAmount.Normal -> {
                       fillMarkingForNormal(otype, outputPlace, transferAmount)
                    }
                    ArcMultiplicityTypeA.TransferAmount.Variable -> {
                        fillMarkingVariable(otype, outputPlace)
                    }
                    is ArcMultiplicityTypeA.TransferAmount.Wrong -> {
                        throw IllegalStateException("how did this pass checks?")
                    }
                }
            }
        }
        return outputMarking
    }

    private fun getTypeToPlaces(places : List<Place>) : MutableMap<ObjectType, MutableList<Place>> {
        return places.fold(mutableMapOf()) { accum, item ->
            accum.getOrPut(item.type) { mutableListOf() }.add(item)
            accum
        }
    }

    private fun getTokenAmountForTypeToPlaces(
        typeToPlaces: MutableMap<ObjectType, MutableList<Place>>
    ): MutableMap<ObjectType, ArcMultiplicityTypeA.TransferAmount> {
        val transitionArcs = arcs[transition]

        return typeToPlaces.keys.fold(mutableMapOf()) { accum, ot ->
            val placeTransitionArcs = typeToPlaces[ot]!!.map { transitionArcs[it]!! }
            var sum = arcMultiplicity.getAbstractTransferAmount(placeTransitionArcs.first())

            for (i in 1 .. placeTransitionArcs.lastIndex) {
                sum += arcMultiplicity.getAbstractTransferAmount(placeTransitionArcs[i])
            }

            accum[ot] = sum

            accum
        }
    }

    private fun checkOtOfInputAndOutput(inputObjectTypes : Collection<ObjectType>, outputObjectTypes : Collection<ObjectType>) {
        require(inputObjectTypes.size == outputObjectTypes.size) {
            "input types doesn't equal to output types"
        }
    }

    private fun checkTransferAmountsMatch(inputTotalByType : MutableMap<ObjectType, ArcMultiplicityTypeA.TransferAmount>,
                                          outputTotalByType : MutableMap<ObjectType, ArcMultiplicityTypeA.TransferAmount>) {
        for (otype in inputTotalByType.keys) {
            val inputTotal = inputTotalByType[otype]
            val outputTotal = outputTotalByType[otype]

            require(inputTotal == outputTotal) {
                "input with total [ $inputTotal ] cannot produce exact amount of tokens as output [ $outputTotal ] as required by output for type $otype"
            }
        }
    }
}
