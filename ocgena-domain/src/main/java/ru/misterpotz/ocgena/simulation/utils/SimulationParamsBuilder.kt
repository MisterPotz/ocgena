package simulation.utils

import config.GenerationConfig
import model.*
import model.time.IntervalFunction
import model.utils.OcNetCreator
import ru.misterpotz.ocgena.collections.objects.ImmutableObjectMarking
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.registries.PlaceObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.token_generation.ObjectMarkingFromPlainCreator
import simulation.*

open class SimulationParamsBuilder(
    private val ocNet: StaticCoreOcNet
) {
    private var generationConfig: GenerationConfig? = null
    private var placeTypeRegistry: PlaceTypeRegistry? = null
    private var initialMarking : ImmutableObjectMarking? = null
    private var timeIntervalFunction : IntervalFunction? = null
    private var randomSeed : Int? = null
    private var useRandom : Boolean = true
    private var placeObjectTypeRegistry : PlaceObjectTypeRegistry? = null
    private var ocNetType : OcNetType? = null
    private var nodeToLabelRegistry : NodeToLabelRegistry? = null

    fun withLabelMapping(nodeToLabelRegistry: NodeToLabelRegistry) : SimulationParamsBuilder {
        this.nodeToLabelRegistry = nodeToLabelRegistry
        return this
    }

    fun withOcNetType(ocNetType: OcNetType) : SimulationParamsBuilder {
        this.ocNetType = ocNetType
        return this
    }

    fun withPlaceTyping(placeObjectTypeRegistry: PlaceObjectTypeRegistry): SimulationParamsBuilder {
        this.placeObjectTypeRegistry = placeObjectTypeRegistry
        return this
    }

    fun withPlaceTypingAndInitialMarking(placeObjectTypeRegistry: PlaceObjectTypeRegistry, plainMarking: PlainMarking): SimulationParamsBuilder {
        this.placeObjectTypeRegistry = placeObjectTypeRegistry
        val plainToObjectTokenGenerator = ObjectMarkingFromPlainCreator(
            plainMarking = plainMarking,
            placeTyping = placeObjectTypeRegistry,
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
            labelMapping = nodeToLabelRegistry ?: NodeToLabelRegistry(),
            generationConfig = generationConfig
        )
    }
}
