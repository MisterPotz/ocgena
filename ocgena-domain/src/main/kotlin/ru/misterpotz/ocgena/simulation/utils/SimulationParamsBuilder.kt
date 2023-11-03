package simulation.utils

import ru.misterpotz.ocgena.simulation.config.GenerationConfig
import model.*
import ru.misterpotz.ocgena.simulation.config.IntervalFunction
import model.utils.OcNetCreator
import ru.misterpotz.marking.plain.PlainMarking
import ru.misterpotz.ocgena.ocnet.StaticCoreOcNetScheme
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import ru.misterpotz.simulation.config.SimulationConfig
import ru.misterpotz.simulation.token_generation.ObjectMarkingFromPlainCreator
import simulation.*

open class SimulationParamsBuilder(
    private val ocNet: StaticCoreOcNetScheme
) {
    private var generationConfig: GenerationConfig? = null
    private var placeTypeRegistry: PlaceTypeRegistry? = null
    private var initialMarking : ru.misterpotz.ocgena.collections.objects.ImmutablePlaceToObjectMarking? = null
    private var timeIntervalFunction : IntervalFunction? = null
    private var randomSeed : Int? = null
    private var useRandom : Boolean = true
    private var placeToObjectTypeRegistry : PlaceToObjectTypeRegistry? = null
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

    fun withPlaceTyping(placeToObjectTypeRegistry: PlaceToObjectTypeRegistry): SimulationParamsBuilder {
        this.placeToObjectTypeRegistry = placeToObjectTypeRegistry
        return this
    }

    fun withPlaceTypingAndInitialMarking(placeToObjectTypeRegistry: PlaceToObjectTypeRegistry, plainMarking: PlainMarking): SimulationParamsBuilder {
        this.placeToObjectTypeRegistry = placeToObjectTypeRegistry
        val plainToObjectTokenGenerator = ObjectMarkingFromPlainCreator(
            plainMarking = plainMarking,
            placeTyping = placeToObjectTypeRegistry,
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
            ocNetScheme = ocNet,
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
