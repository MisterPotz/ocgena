package ru.misterpotz.simulation.queue

import config.GenerationConfig
import config.TimeRange
import model.PlaceId
import model.PlaceTyping
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.objects.ObjectMarkingDelta
import ru.misterpotz.marking.objects.ObjectToken
import ru.misterpotz.marking.objects.Time
import simulation.TokenGenerationTimeSelector

class NormalGenerationQueue(
    private val generationConfig: GenerationConfig,
    private val nextTimeSelector: TokenGenerationTimeSelector,
    val placeTyping: PlaceTyping,
    private val tokenGenerationFacade: TokenGenerationFacade,
    private val defaultGenerationInterval: TimeRange? = null
) : GenerationQueue {

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

    override fun shiftTime(time: Time) {
        placeGenerators.forEach {
            it.value.shiftTime(time)
        }
    }

    private fun generateTokenIfCan(placeId: PlaceId): ObjectToken? {
        val generator = placeGenerators[placeId]!!

        return if (generator.mustGenerateNow()) {
            val placeType = placeTyping[placeId]
            generator.markAsNewGenerated()
            tokenGenerationFacade.generate(placeType)
        } else null
    }

    override fun generateTokensAsMarkingAndReplan(): ObjectMarkingDelta? {
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
            return ImmutableObjectMarking(map)
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
        fun shiftTime(time: Time) {
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
