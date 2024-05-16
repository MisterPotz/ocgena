package ru.misterpotz.ocgena.simulation_old.generator.impl

import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.simulation_old.config.TokenGenerationConfig
import ru.misterpotz.ocgena.simulation_old.config.Period
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation_old.Time
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenGenerationFacade
import ru.misterpotz.ocgena.simulation_old.generator.NewTokenTimeBasedGenerator
import ru.misterpotz.ocgena.simulation_old.generator.NewTokensGenerationTimeGenerator

class NormalNewTokenTimeBasedGenerator(
    private val tokenGenerationConfig: TokenGenerationConfig,
    private val nextTimeSelector: NewTokensGenerationTimeGenerator,
    val placeToObjectTypeRegistry: PlaceToObjectTypeRegistry,
    private val newTokenGenerationFacade: NewTokenGenerationFacade,
    private val defaultGenerationInterval: Period? = null
) : NewTokenTimeBasedGenerator {

    private val placeGenerators: Map<PetriAtomId, PlaceGenerator> = buildMap {
        for ((i, value) in tokenGenerationConfig.placeIdToGenerationTarget.placesToTokens) {
            put(
                i, PlaceGenerator(
                    mustTotallyGenerate = value,
                    timeRange = tokenGenerationConfig.defaultPeriod
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

    private fun generateTokenIfCan(placeId: PetriAtomId): Boolean {
        val generator = placeGenerators[placeId]!!

        return if (generator.mustGenerateNow()) {
            val objectTypeId = placeToObjectTypeRegistry[placeId]
            generator.markAsNewGenerated()
            true
        } else {
            false
        }
    }

    override fun generateFictiveTokensAsMarkingSchemeAndReplan(): MarkingScheme? {
        val map = buildMap {
            for ((id, generator) in placeGenerators) {

                val considerTokenAsGenerated = generateTokenIfCan(id)
                val delta = if (considerTokenAsGenerated) 1 else 0
                val newValue = getOrPut(id) { 0 } as Int + delta
                put(id, newValue)

                if (generator.mustPlan()) {
                    generator.plan(nextTimeSelector.get(generator.timeRange))
                }
            }
        }
        if (map.isNotEmpty()) {
            return MarkingScheme(map)
        }
        return null
    }

    override fun getTimeUntilNextPlanned(): Time? {
        var minTime: Time? = null

        for ((_, generator) in placeGenerators) {
            if (generator.hasPlannedToken()) {
                minTime = minTime
                    ?.coerceAtMost(generator.nextGenerationHappensIn!!)
                    ?: generator.nextGenerationHappensIn!!
            }
        }
        return minTime
    }

    override fun planTokenGenerationForEveryone() {
        for ((_, generator) in placeGenerators) {
            if (!generator.hasPlannedToken() && generator.mustPlan()) {
                generator.plan(nextTimeSelector.get(generator.timeRange))
            }
        }
    }

    private class PlaceGenerator(
        val mustTotallyGenerate: Int,
        val timeRange: Period,
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
