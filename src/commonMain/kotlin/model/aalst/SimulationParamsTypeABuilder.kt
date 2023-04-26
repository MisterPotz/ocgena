package model.aalst

import model.time.IntervalFunction
import model.ObjectMarking
import model.PlaceTyping
import model.StaticCoreOcNet
import simulation.ObjectMarkingFromPlainCreator
import simulation.PlainMarking
import simulation.SimulationParams
import simulation.typea.SimulatableOcNetTypeA

class SimulationParamsTypeABuilder(
    private val ocNet: StaticCoreOcNet
) {
    private var initialMarking : ObjectMarking? = null
    private var timeIntervalFunction : IntervalFunction? = null
    private var randomSeed : Long? = null
    private var useRandom : Boolean = true

    fun withInitialMarking(plainMarking: PlainMarking): SimulationParamsTypeABuilder {
        val plainToObjectTokenGenerator = ObjectMarkingFromPlainCreator(
            plainMarking = plainMarking,
            placeTyping = PlaceTyping(),
            objectTypes = ocNet.objectTypes
        )
        initialMarking = plainToObjectTokenGenerator.create()
        return this
    }

    fun withTimeIntervals(timeIntervalFunction: IntervalFunction): SimulationParamsTypeABuilder {
        this.timeIntervalFunction = timeIntervalFunction
        return this
    }

    fun withRandomSeed(randomSeed : Long?) : SimulationParamsTypeABuilder {
        this.randomSeed = randomSeed
        return this
    }

    fun useRandom(useRandom: Boolean): SimulationParamsTypeABuilder {
        this.useRandom = useRandom
        return this
    }

    fun build() : SimulationParams {
        val ocNet = SimulatableOcNetTypeA(
            coreOcNet = ocNet,
            arcMultiplicity = ArcMultiplicityTypeA(arcs = ocNet.arcs),
            intervalFunction = timeIntervalFunction!!
        )
        return SimulationParams(
            templateOcNet = ocNet,
            initialMarking = initialMarking!!,
            timeoutSec = null,
            randomSeed = randomSeed,
            useRandom = useRandom
        )
    }
}
