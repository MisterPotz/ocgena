package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation_v2.entities.InputArcWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TransitionWrapper

enum class GroupingStrategy {
    ByType,
    ByTypeAndArc
}

class TokenArcFlowSnapshotFactory(
    private val transitionWrapper: TransitionWrapper,
    private val consumed: TokenSlice,
) {
    private val groupedByType by lazy(LazyThreadSafetyMode.NONE) {
        val map: MutableMap<CompoundKey, MutableList<ConsumedPlaceRecord>> = mutableMapOf()

        consumed.byPlaceIterator().forEach { (place, tokens) ->
            val inputArc = transitionWrapper.findInputArcByPlace(place)

            val key = makeCompoundKey(GroupingStrategy.ByType, place.objectType, inputArc)

            val record = ConsumedPlaceRecord(
                amount = tokens.size,
                tokens = tokens.toList(),
                objectType = place.objectType,
                arc = inputArc
            )

            map.getOrPut(key) {
                mutableListOf()
            }.add(record)
        }
        SimpleSnapshot(map, GroupingStrategy.ByType)
    }

    fun getGrouped(groupingStrategy: GroupingStrategy): Snapshot {
        return when (groupingStrategy) {
            GroupingStrategy.ByType -> groupedByType
            GroupingStrategy.ByTypeAndArc -> throw NotImplementedError()
        }
    }

    data class ConsumedPlaceRecord(
        val amount: Int,
        val tokens: List<TokenWrapper>?,
        val objectType: ObjectType,
        val arc: InputArcWrapper,
    )

    interface Snapshot {
        fun getRecords(
            objectType: ObjectType,
            inputArcWrapper: InputArcWrapper
        ): List<ConsumedPlaceRecord>
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
        private val groupedRecords: Map<CompoundKey, List<ConsumedPlaceRecord>>,
        private val groupingStrategy: GroupingStrategy,
    ) : Snapshot {

        val iterable = groupedRecords

        override fun getRecords(
            objectType: ObjectType,
            inputArcWrapper: InputArcWrapper,
        ): List<ConsumedPlaceRecord> {
            val compoundKey = makeCompoundKey(groupingStrategy, objectType, inputArcWrapper)
            return groupedRecords[compoundKey]!!
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