package ru.misterpotz.ocgena.simulation_v2.entities_storage

import ru.misterpotz.ocgena.ocnet.primitives.arcs.ArcMeta
import ru.misterpotz.ocgena.simulation.ObjectType
import ru.misterpotz.ocgena.simulation_v2.algorithm.simulation.PlaceWrapper
import ru.misterpotz.ocgena.simulation_v2.entities.TokenWrapper

class TokenArcFlowSnapshot(
    val records: List<ConsumedPlaceRecord>
) {
    private val groupedByType by lazy(LazyThreadSafetyMode.NONE) {
        val map: MutableMap<SimpleGroupedRecords.CompoundKey, MutableList<ConsumedPlaceRecord>> = mutableMapOf()
        for (record in records) {
            val key = SimpleGroupedRecords.CompoundKey(arcMeta = null, objectType = record.objectType)

            map.getOrPut(key) {
                mutableListOf()
            }.add(record)
        }
        @Suppress("UNCHECKED_CAST")
        (SimpleGroupedRecords(
        map as Map<Any, List<ConsumedPlaceRecord>>
    ))
    }

    fun getGrouped(groupingStrategy: GroupingStrategy): GroupedRecords {
        return when (groupingStrategy) {
            GroupingStrategy.ByType -> groupedByType
        }
    }

    data class ConsumedPlaceRecord(
        val amount: Int,
        val tokens: List<TokenWrapper>?,
        val objectType: ObjectType,
        val place: PlaceWrapper,
        val arcMeta: ArcMeta
    )

    interface GroupedRecords {
        fun getRecords(
            objectType: ObjectType,
            arcMeta: ArcMeta
        ): List<ConsumedPlaceRecord>
    }

    enum class GroupingStrategy {
        ByType
    }

    private class SimpleGroupedRecords(
        val groupedRecords: Map<Any, List<ConsumedPlaceRecord>>
    ) : GroupedRecords {
        override fun getRecords(objectType: ObjectType, arcMeta: ArcMeta): List<ConsumedPlaceRecord> {
            val compoundKey = CompoundKey(arcMeta, objectType)

            return groupedRecords[compoundKey]!!
        }

        data class CompoundKey(val arcMeta: ArcMeta?, val objectType: ObjectType?)
    }
}