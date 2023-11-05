package ru.misterpotz.ocgena.simulation.generator.impl

import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingDelta
import ru.misterpotz.ocgena.simulation.config.GenerationConfig
import ru.misterpotz.ocgena.simulation.config.TimeRange
import ru.misterpotz.ocgena.ocnet.PlaceId
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectToken
import ru.misterpotz.ocgena.simulation.Time
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerationFacade
import ru.misterpotz.ocgena.simulation.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation.generator.NewTokensGenerationTimeGenerator

class NormalNewTokenTimeBasedGenerator(
    private val generationConfig: GenerationConfig,
    private val nextTimeSelector: NewTokensGenerationTimeGenerator,
    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val newTokenTimeBasedGenerationFacade: NewTokenTimeBasedGenerationFacade,
    private val defaultGenerationInterval: TimeRange? = null
) : NewTokenTimeBasedGenerator {

    private val placeGenerators: Map<PlaceId, PlaceGenerator> = buildMap {
        for ((i, value) in generationConfig.placeIdToGenerationTarget) {
            put(
                i, PlaceGenerator(
                    mustTotallyGenerate = value,
                    timeRange = generationConfig.defaultGeneration
                        ?: defaultGenerationInterval
                        ?: continue,
                    nextGenerationHappensIn = null
                )
            )
        }
    }

    override fun increaseTime(time: Time) {
        placeGenerators.forEach {
            it.value.increaseTime(time)
        }
    }

    private fun generateTokenIfCan(placeId: PlaceId): ObjectToken? {
        val generator = placeGenerators[placeId]!!

        return if (generator.mustGenerateNow()) {
            val objectTypeId = placeToObjectTypeRegistry[placeId]
            generator.markAsNewGenerated()
            newTokenTimeBasedGenerationFacade.generate(objectTypeId)
        } else null
    }

    override fun generateTokensAsMarkingAndReplan(): PlaceToObjectMarkingDelta? {
        val map = buildMap {
            for ((id, generator) in placeGenerators) {

                generateTokenIfCan(id)?.let { objectToken ->
                    put(id, sortedSetOf(objectToken.id))
                }

                if (generator.mustPlan()) {
                    generator.plan(nextTimeSelector.get(generator.timeRange))
                }
            }
        }
        if (map.isNotEmpty()) {
            return ImmutablePlaceToObjectMarking(map)
        }
        return null
    }

    override fun getTimeUntilNextPlanned(): Time? {
        var minTime: Time? = null

        for ((id, generator) in placeGenerators) {
            if (generator.hasPlannedToken()) {
                minTime = minTime
                    ?.coerceAtMost(generator.nextGenerationHappensIn!!)
                    ?: generator.nextGenerationHappensIn!!
            }
        }
        return minTime
    }

    override fun planTokenGenerationForEveryone() {
        for ((id, generator) in placeGenerators) {
            if (!generator.hasPlannedToken() && generator.mustPlan()) {
                generator.plan(nextTimeSelector.get(generator.timeRange))
            }
        }
    }

    private class PlaceGenerator(
        val mustTotallyGenerate: Int,
        val timeRange: TimeRange,
        var nextGenerationHappensIn: Time?,
    ) {
        var generated: Int = 0
        fun increaseTime(time: Time) {
            val nextGenerationHappensIn = nextGenerationHappensIn ?: return
            this.nextGenerationHappensIn = (nextGenerationHappensIn - time).coerceAtLeast(0)
        }

        fun hasPlannedToken(): Boolean {
            return nextGenerationHappensIn != null
        }

        fun mustGenerateNow(): Boolean {
            return nextGenerationHappensIn == 0L
        }

        fun mustPlan(): Boolean {
            return generated < mustTotallyGenerate && !hasPlannedToken()
        }

        fun plan(inTime: Time) {
            nextGenerationHappensIn = inTime
        }

        fun markAsNewGenerated(): Boolean {
            if (nextGenerationHappensIn != null) {
                nextGenerationHappensIn = null
                generated++
                return true
            }
            return false
        }
    }
}
