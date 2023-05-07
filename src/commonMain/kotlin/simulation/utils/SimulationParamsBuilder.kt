package simulation.utils

import model.*
import model.time.IntervalFunction
import model.utils.OcNetCreator
import simulation.*

class SimulationParamsBuilder(
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

    fun withInitialMarking(plainMarking: PlainMarking): SimulationParamsBuilder {
        val plainToObjectTokenGenerator = ObjectMarkingFromPlainCreator(
            plainMarking = plainMarking,
            placeTyping = this.placeTyping!!,
            generator = objectTokenGenerator
        )
        initialMarking = plainToObjectTokenGenerator.create()
        return this
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
