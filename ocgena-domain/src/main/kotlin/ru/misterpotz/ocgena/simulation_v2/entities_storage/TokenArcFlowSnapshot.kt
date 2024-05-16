package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.simulation_old.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.castVar

enum class GroupingStrategy {
    ByType,
    ByTypeAndArc
}

class TokenArcFlowSnapshotFactory(
    private val transitionWrapper: TransitionWrapper,
    private val consumed: TokenSlice,
) {
    private val groupedByType by lazy(LazyThreadSafetyMode.NONE) {
        // merge token datas here to aggregate in snapshot

        val objTypeToToken = mutableMapOf<ObjectType, MutableList<TokenWrapper>>()
        val objTypeToAmount = mutableMapOf<ObjectType, Int>()
        val variableValues = mutableMapOf<String, Int>()

        consumed.byPlaceIterator().forEach { (place, tokens) ->
            for (token in tokens) {
                objTypeToToken.getOrPut(token.objectType) { mutableListOf() }.add(token)
            }
            objTypeToAmount[place.objectType] =
                objTypeToAmount.getOrPut(place.objectType) { 0 } + consumed.amountAt(place)

            val inputArc = transitionWrapper.findInputArcByPlace(place)

            if (inputArc.consumptionSpec is InputArcWrapper.ConsumptionSpec.Variable) {
                variableValues[inputArc.consumptionSpec.castVar().variableName] = consumed.amountAt(place)
            }
        }

        val objTypeToTokenData = objTypeToToken.mapValues {
            TokenData(
                objectType = it.key,
                amount = objTypeToAmount[it.key]!!,
                tokens = it.value
            )
        }

        SimpleSnapshot(
            groupedRecords = objTypeToTokenData,
            variablesValues = variableValues,
        )
    }

    fun getGrouped(groupingStrategy: GroupingStrategy): Snapshot {
        return when (groupingStrategy) {
            GroupingStrategy.ByType -> groupedByType
            GroupingStrategy.ByTypeAndArc -> throw NotImplementedError()
        }
    }

    data class TokenData(
        val objectType: ObjectType,
        val amount: Int,
        val tokens: List<TokenWrapper>
    )

    interface Snapshot {
        fun getVariableValue(value: String): Int
        fun getGroup(type: ObjectType): TokenData
        val allTokens: Iterable<TokenWrapper>
    }

    companion object {
        fun makeCompoundKey(
            groupingStrategy: GroupingStrategy,
            objectType: ObjectType,
            inputArcWrapper: InputArcWrapper?
        ): CompoundKey {
            return when (groupingStrategy) {
                GroupingStrategy.ByType -> CompoundKey(objectType, null)
                GroupingStrategy.ByTypeAndArc -> CompoundKey(
                    objectType,
                    inputArcWrapper
                )
            }
        }
    }

    class SimpleSnapshot(
        private val variablesValues: Map<String, Int>,
        private val groupedRecords: Map<ObjectType, TokenData>,
    ) : Snapshot {
        override fun getVariableValue(value: String): Int {
            return variablesValues[value]!!
        }

        override fun getGroup(type: ObjectType): TokenData {
            return groupedRecords[type]!!
        }

        override val allTokens: Iterable<TokenWrapper> = groupedRecords.values.flatMap {
            it.tokens
        }
    }

    data class CompoundKey(
        val objectType: ObjectType?,
        val inputArcWrapper: InputArcWrapper?,
    ) : Comparable<CompoundKey> {
        override fun compareTo(other: CompoundKey): Int {
            return comparator.compare(this, other)
        }

        companion object {
            val comparator = compareBy<CompoundKey>(
                {
                    it.objectType
                },
                {
                    it.inputArcWrapper
                },
            )
        }
    }
}