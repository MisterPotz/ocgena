package simulation.utils

import config.GenerationConfig
import model.*
import model.time.IntervalFunction
import model.utils.OcNetCreator
import ru.misterpotz.marking.objects.ImmutableObjectMarking
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.token_generation.ObjectMarkingFromPlainCreator
import simulation.*

open class SimulationParamsBuilder(
    private val ocNet: StaticCoreOcNet
) {
    private var generationConfig: GenerationConfig? = null
    private var inputOutputPlaces: InputOutputPlaces? = null
    private var initialMarking : ImmutableObjectMarking? = null
    private var timeIntervalFunction : IntervalFunction? = null
    private var randomSeed : Int? = null
    private var useRandom : Boolean = true
    private var placeTyping : PlaceTyping? = null
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

    fun withRandomSeed(randomSeed : Int?) : SimulationParamsBuilder {
        this.randomSeed = randomSeed
        return this
    }

    fun useRandom(useRandom: Boolean): SimulationParamsBuilder {
        this.useRandom = useRandom
        return this
    }

    fun build() : SimulationConfig {
        val creator = OcNetCreator(
            coreOcNet = ocNet,
            timeIntervalFunction = timeIntervalFunction!!
        )
        val ocNet = creator.create(ocNetType!!)

        return SimulationConfig(
            templateOcNet = ocNet,
            initialMarking = initialMarking!!,
            timeoutSec = null,
            randomSeed = randomSeed,
            useRandom = useRandom,
            objectTokenGenerator = objectTokenGenerator,
            labelMapping = labelMapping ?: LabelMapping(),
            generationConfig = generationConfig
        )
    }
}
