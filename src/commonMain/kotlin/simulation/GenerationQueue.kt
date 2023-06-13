package simulation

import config.GenerationConfig
import config.TimeRange
import config.TimeRangeClass
import model.ImmutableObjectMarking
import model.PlaceId
import model.PlaceTyping
import model.Time
import simulation.time.TokenGenerationTimeSelector

interface GenerationQueue {
    fun shiftTime(time: Time)
    fun generateTokensAsMarkingAndReplan() : ImmutableObjectMarking?
    fun getTimeUntilNextPlanned() : Time?

    fun planTokenGenerationForEveryone()
}
class DumbGenerationQueue() : GenerationQueue{
    override fun shiftTime(time: Time) {

    }

    override fun generateTokensAsMarkingAndReplan(): ImmutableObjectMarking? {
        return null
    }

    override fun getTimeUntilNextPlanned(): Time? {
        return null
    }

    override fun planTokenGenerationForEveryone() {

    }

}
class NormalGenerationQueue(
    private val generationConfig: GenerationConfig,
    private val nextTimeSelector: TokenGenerationTimeSelector,
    val placeTyping: PlaceTyping,
    private val tokenGenerator: ObjectTokenGenerator,
    private val defaultGenerationInterval : TimeRangeClass? = null
) : GenerationQueue {

    private val placeGenerators : Map<PlaceId, PlaceGenerator> = buildMap {
        for ((i, value) in generationConfig.placeIdToGenerationTarget) {
            put(i, PlaceGenerator(
                placeId = i,
                mustTotallyGenerate = value,
                timeRange = generationConfig.defaultGeneration
                    ?: defaultGenerationInterval
                    ?: continue,
                nextGenerationHappensIn = null
            ))
        }
    }

    override fun shiftTime(time: Time) {
        placeGenerators.forEach {
            it.value.shiftTime(time)
        }
    }

    override fun generateTokensAsMarkingAndReplan() : ImmutableObjectMarking? {
        val map = buildMap {
            for ((id, generator) in placeGenerators) {
                if (generator.mustGenerateNow()) {
                    val placeType = placeTyping[id]
                    put(id, setOf(tokenGenerator.generate(placeType)))
                    generator.markAsNewGenerated()
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

    override fun getTimeUntilNextPlanned() : Time? {
        var minTime : Time? = null

        for ((id, generator) in placeGenerators) {
            if (generator.hasPlannedToken()) {
                minTime = if (minTime == null) {
                    generator.nextGenerationHappensIn!!
                } else {
                    minTime.coerceAtMost(generator.nextGenerationHappensIn!!)
                }
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

    class PlaceGenerator(
        val placeId : String,
        val mustTotallyGenerate : Int,
        val timeRange: TimeRange,
        var nextGenerationHappensIn: Time?,
    ) {
        var generated : Int = 0
        fun shiftTime(time: Time) {
            val nextGenerationHappensIn = nextGenerationHappensIn ?: return
            this.nextGenerationHappensIn = (nextGenerationHappensIn - time).coerceAtLeast(0)
        }

        fun hasPlannedToken(): Boolean {
            return nextGenerationHappensIn != null
        }

        fun mustGenerateNow() : Boolean {
            return nextGenerationHappensIn == 0
        }

        fun mustPlan() : Boolean {
            return generated < mustTotallyGenerate && !hasPlannedToken()
        }

        fun plan(inTime : Time)  {
            nextGenerationHappensIn = inTime
        }

        fun markAsNewGenerated() : Boolean {
            if (nextGenerationHappensIn != null) {
                nextGenerationHappensIn = null
                generated++
                return true
            }
            return false
        }
    }
}
