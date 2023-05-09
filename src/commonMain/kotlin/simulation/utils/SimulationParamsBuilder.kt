package simulation.utils

import model.*
import model.time.IntervalFunction
import model.utils.OcNetCreator
import simulation.*

fun createParams(ocNet: StaticCoreOcNet, config: ProcessedSimulationConfig) : SimulationParams {
    val simulationParamsBuilder = SimulationParamsBuilder(ocNet)

    simulationParamsBuilder
        .withPlaceTypingAndInitialMarking(config.placeTyping, config.initialPlainMarking)
        .withInputOutput(config.inputOutputPlaces)
        .withLabelMapping(config.labelMapping)
        .withOcNetType(config.type)
        .withTimeIntervals(config.intervalFunction)
        .withRandomSeed(42L)
        .useRandom(false)
    return simulationParamsBuilder.build()
}

open class SimulationParamsBuilder(
    private val ocNet: StaticCoreOcNet
) {
    private var inputOutputPlaces: InputOutputPlaces? = null
    private var initialMarking : ObjectMarking? = null
    private var timeIntervalFunction : IntervalFunction? = null
    private var randomSeed : Long? = null
    private var useRandom : Boolean = true
    private var placeTyping : PlaceTyping? = null
    private var objectTokenGenerator = ObjectTokenGenerator()
    private var ocNetType : OcNetType? = null
    private var labelMapping : LabelMapping? = null

    fun withLabelMapping(labelMapping: LabelMapping) : SimulationParamsBuilder {
        this.labelMapping = labelMapping
        return this
    }

    fun withOcNetType(ocNetType: OcNetType) : SimulationParamsBuilder {
        this.ocNetType = ocNetType
        return this
    }

    fun withPlaceTyping(placeTyping: PlaceTyping): SimulationParamsBuilder {
        this.placeTyping = placeTyping
        return this
    }

    fun withPlaceTypingAndInitialMarking(placeTyping: PlaceTyping, plainMarking: PlainMarking): SimulationParamsBuilder {
        this.placeTyping = placeTyping
        val plainToObjectTokenGenerator = ObjectMarkingFromPlainCreator(
            plainMarking = plainMarking,
            placeTyping = placeTyping,
            generator = objectTokenGenerator
        )
        initialMarking = plainToObjectTokenGenerator.create()
        return this@SimulationParamsBuilder
    }

    fun withTimeIntervals(timeIntervalFunction: IntervalFunction): SimulationParamsBuilder {
        this.timeIntervalFunction = timeIntervalFunction
        return this
    }

    fun withRandomSeed(randomSeed : Long?) : SimulationParamsBuilder {
        this.randomSeed = randomSeed
        return this
    }

    fun useRandom(useRandom: Boolean): SimulationParamsBuilder {
        this.useRandom = useRandom
        return this
    }

    fun build() : SimulationParams {
        val creator = OcNetCreator(
            coreOcNet = ocNet,
            timeIntervalFunction = timeIntervalFunction!!
        )
        val ocNet = creator.create(ocNetType!!)

        return SimulationParams(
            templateOcNet = ocNet,
            initialMarking = initialMarking!!,
            timeoutSec = null,
            randomSeed = randomSeed,
            useRandom = useRandom,
            objectTokenGenerator = objectTokenGenerator,
            labelMapping = labelMapping ?: LabelMapping()
        )
    }

    fun withInputOutput(build: InputOutputPlaces): SimulationParamsBuilder {
        this.inputOutputPlaces = build
        return this
    }
}
