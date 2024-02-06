package ru.misterpotz.ocgena.testing

import com.charleskorn.kaml.Yaml
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.misterpotz.DBLogger
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.ocnet.utils.makeObjTypeId
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.SimulationTask
import ru.misterpotz.ocgena.simulation.config.MarkingScheme
import ru.misterpotz.ocgena.simulation.config.Period
import ru.misterpotz.ocgena.simulation.config.SimulationConfig
import ru.misterpotz.ocgena.simulation.config.TokenGenerationConfig
import ru.misterpotz.ocgena.simulation.config.original.Duration
import ru.misterpotz.ocgena.simulation.config.original.TimeUntilNextInstanceIsAllowed
import ru.misterpotz.ocgena.simulation.config.original.TransitionInstanceTimes
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.di.SimulationComponentDependencies
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation.stepexecutor.SimpleDeterminedTransitionSequenceProvider
import ru.misterpotz.ocgena.validation.OCNetChecker
import simulation.random.RandomUseCase
import java.util.*
import kotlin.random.Random

val DOMAIN_COMPONENT = domainComponent()
val USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME = true

typealias OCNetBuildingCodeBlock = OCNetBuilder.AtomDefinitionBlock.() -> Unit

fun OCNetBuilder.AtomDefinitionBlock.install(oCNetBuildingCodeBlock: OCNetBuildingCodeBlock) {
    oCNetBuildingCodeBlock.invoke(this)
}

fun OCNetBuildingCodeBlock.installOnto(builder: OCNetBuilder.AtomDefinitionBlock) {
    builder.install(this)
}

fun buildOCNet(atomDefinitionBlock: OCNetBuildingCodeBlock): OCNetStruct {
    val ocNet = OCNetBuilder().defineAtoms(atomDefinitionBlock)
    val errors = OCNetChecker(ocNet).checkConsistency()


    assert(
        errors.isEmpty()
    ) {
        "ocNet is null, detected errors: ${errors.prettyPrint()}"
    }

    return ocNet
}

fun buildSimplestOCNetNoVar(): OCNetStruct {
    return buildOCNet {
        "p1".p { input }
            .arc("t1".t)
            .arc("p2".p { output })
    }
}

fun defaultSimConfigOriginal(
    ocNet: OCNetStruct,
): SimulationConfig {
    return SimulationConfig(
        ocNet,
        initialMarking = MarkingScheme.of {
            put("p1", 10)
        },
        transitionsSpec = TransitionsOriginalSpec(
            defaultTransitionTimeSpec = TransitionInstanceTimes(
                duration = Duration(2..10),
                timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(10..100)
            )
        ),
        randomSeed = 42,
        nodeToLabelRegistry = NodeToLabelRegistry(),
        tokenGeneration = TokenGenerationConfig(
            defaultPeriod = Period(100..120),
            placeIdToGenerationTarget = MarkingScheme.of {
                put("p1", 15)
            }
        ),
        ocNetType = OcNetType.AALST,
        simulationSemantics = SimulationSemantics(type = SimulationSemanticsType.ORIGINAL)
    )
}

fun defaultSimConfigTimePN(
    ocNet: OCNetStruct,
): SimulationConfig {
    return SimulationConfig(
        ocNet,
        initialMarking = MarkingScheme.of {
            put("p1", 10)
        },
        transitionsSpec = TransitionsOriginalSpec(
            defaultTransitionTimeSpec = TransitionInstanceTimes(
                duration = Duration(2..10),
                timeUntilNextInstanceIsAllowed = TimeUntilNextInstanceIsAllowed(10..100)
            )
        ),
        randomSeed = 42,
        nodeToLabelRegistry = NodeToLabelRegistry(),
        tokenGeneration = TokenGenerationConfig(
            defaultPeriod = Period(100..120),
            placeIdToGenerationTarget = MarkingScheme.of {
                put("p1", 15)
            }
        ),
        ocNetType = OcNetType.AALST,
        simulationSemantics = SimulationSemantics(type = SimulationSemanticsType.SIMPLE_TIME_PN)
    )
}

fun simulationComponentDependencies(
    dbLogger: DBLogger? = null,
    domainComponent: DomainComponent = domainComponent()
): SimulationComponentDependencies {
    return object : SimulationComponentDependencies {
        val dbLogger: DBLogger = dbLogger ?: mockk(relaxed = true, relaxUnitFun = true)
        override val json: Json = domainComponent.json()
        override val yaml: Yaml = domainComponent.yaml()

        override fun dbLogger(): DBLogger = this.dbLogger

    }
}

fun domainComponent(): DomainComponent {
    return DomainComponent.create()
}

fun simComponent(
    simulationConfig: SimulationConfig,
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    randomInstances: Map<RandomUseCase, Random> = mutableMapOf(),
    determinedTransitionSequenceProvider: SimpleDeterminedTransitionSequenceProvider = SimpleDeterminedTransitionSequenceProvider(),
    dbLogger: DBLogger? = null
): SimulationComponent {
    return SimulationComponent.defaultCreate(
        simulationConfig = simulationConfig,
        componentDependencies = simulationComponentDependencies(dbLogger),
        randomInstances = randomInstances,
        developmentDebugConfig = developmentDebugConfig,
        determinedTransitionSequenceProvider = determinedTransitionSequenceProvider
    )
}


fun simComponentOld(
    simulationConfig: SimulationConfig,
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
    randomInstance: Random? = null
): SimulationComponent {
    return SimulationComponent.defaultCreate(
        simulationConfig = simulationConfig,
        componentDependencies = simulationComponentDependencies(),
        randomInstances = listOf(RandomUseCase.TIME_SELECTION to randomInstance).filter {
            it.second != null
        }.associateBy { it.first }
            .mapValues { it.value.second!! },
        developmentDebugConfig = developmentDebugConfig
    )
}

fun simTask(component: SimulationComponent): SimulationTask {
    return component.simulationTask()
}

data class ObjectTokenIdAndType(val objectTokenId: ObjectTokenId, val objectTypeId: ObjectTypeId)


fun ImmutablePlaceToObjectMarking.loggableString(simulationComponent: SimulationComponent): String {
    this as ImmutablePlaceToObjectMarkingMap

    val loggable = LoggableImmutablePlaceToObjectMarkingMap(
        placesToObjectTokens.mapValues {
            it.value.map {
                simulationComponent.objectTokenSet()[it]?.name!!
            }.toSortedSet()
        }
    )
    return loggable.placesToObjectTokens.toString()
}

@Serializable
@SerialName("placeToObject")
private data class LoggableImmutablePlaceToObjectMarkingMap(
    @SerialName("per_place") val placesToObjectTokens: Map<PetriAtomId, SortedSet<String>>,
)

fun String.objTypeId(): ObjectTypeId {
    return if (USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME) {
        makeObjTypeId()
    } else {
        this
    }
}

