package ru.misterpotz.ocgena.testing

import com.charleskorn.kaml.Yaml
import io.mockk.mockk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.misterpotz.Logger
import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.simulation_old.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.ocnet.utils.makeObjTypeId
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation_old.ObjectTokenId
import ru.misterpotz.ocgena.simulation_old.SimulationTask
import ru.misterpotz.ocgena.simulation_old.config.MarkingScheme
import ru.misterpotz.ocgena.simulation_old.config.Period
import ru.misterpotz.ocgena.simulation_old.config.SimulationConfig
import ru.misterpotz.ocgena.simulation_old.config.TokenGenerationConfig
import ru.misterpotz.ocgena.simulation_old.config.original.Duration
import ru.misterpotz.ocgena.simulation_old.config.original.TimeUntilNextInstanceIsAllowed
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionInstanceTimes
import ru.misterpotz.ocgena.simulation_old.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponent
import ru.misterpotz.ocgena.simulation_old.di.SimulationComponentDependencies
import ru.misterpotz.ocgena.simulation_old.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation_old.logging.SimulationDBLogger
import ru.misterpotz.ocgena.simulation_old.logging.fastNoDevSetup
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation_old.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.simulation_old.stepexecutor.SimpleDeterminedTransitionSequenceProvider
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

fun buildOCNet(ocNetType: OcNetType = OcNetType.AALST, atomDefinitionBlock: OCNetBuildingCodeBlock): OCNetStruct {
    val ocNet = OCNetBuilder(ocNetType = ocNetType).defineAtoms(atomDefinitionBlock)
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
    dbLogger: Logger? = null,
    domainComponent: DomainComponent = domainComponent()
): SimulationComponentDependencies {
    return object : SimulationComponentDependencies {
        val dbLogger: Logger = dbLogger ?: mockk(relaxed = true, relaxUnitFun = true)
        override val json: Json = domainComponent.json()
        override val yaml: Yaml = domainComponent.yaml()

        override fun dbLogger(): Logger = this.dbLogger
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
    dbLogger: Logger? = null
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

