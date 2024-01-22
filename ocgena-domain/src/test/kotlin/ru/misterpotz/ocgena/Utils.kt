package ru.misterpotz.ocgena

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertTrue
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarking
import ru.misterpotz.ocgena.collections.ImmutablePlaceToObjectMarkingMap
import ru.misterpotz.ocgena.di.DomainComponent
import ru.misterpotz.ocgena.error.prettyPrint
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.atoms.ArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder
import ru.misterpotz.ocgena.ocnet.utils.prependId
import ru.misterpotz.ocgena.registries.NodeToLabelRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.SimulationTask
import ru.misterpotz.ocgena.simulation.binding.TokenSet
import ru.misterpotz.ocgena.simulation.binding.buffer.TokenGroupedInfo
import ru.misterpotz.ocgena.simulation.config.*
import ru.misterpotz.ocgena.simulation.config.original.Duration
import ru.misterpotz.ocgena.simulation.config.original.TimeUntilNextInstanceIsAllowed
import ru.misterpotz.ocgena.simulation.config.original.TransitionInstanceTimes
import ru.misterpotz.ocgena.simulation.config.original.TransitionsOriginalSpec
import ru.misterpotz.ocgena.simulation.di.SimulationComponent
import ru.misterpotz.ocgena.simulation.logging.DevelopmentDebugConfig
import ru.misterpotz.ocgena.simulation.logging.fastNoDevSetup
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemantics
import ru.misterpotz.ocgena.simulation.semantics.SimulationSemanticsType
import ru.misterpotz.ocgena.validation.OCNetChecker
import java.util.*

val DOMAIN_COMPONENT = domainComponent()

fun buildOCNet(atomDefinitionBlock: OCNetBuilder.AtomDefinitionBlock.() -> Unit): OCNetStruct {
    val ocNet = OCNetBuilder().defineAtoms(atomDefinitionBlock)
    val errors = OCNetChecker(ocNet).checkConsistency()


    assertTrue(
        errors.isEmpty(),
        "ocNet is null, detected errors: ${errors.prettyPrint()}"
    )
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


fun domainComponent(): DomainComponent {
    return DomainComponent.create()
}

fun simComponent(
    simulationConfig: SimulationConfig,
    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup(),
): SimulationComponent {
    return SimulationComponent.defaultCreate(
        simulationConfig = simulationConfig,
        componentDependencies = DOMAIN_COMPONENT,
        developmentDebugConfig = developmentDebugConfig
    )
}

fun simTask(component: SimulationComponent): SimulationTask {
    return component.simulationTask()
}

fun SimulationComponent.facade(): FacadeSim {
    return FacadeSim(
        this,
        task = simulationTask(),
        config = simulationConfig()
    )
}


data class ObjectTokenIdAndType(val objectTokenId: ObjectTokenId, val objectTypeId: ObjectTypeId)

fun Int.withType(objectTypeId: ObjectTypeId): ObjectTokenIdAndType {
    return ObjectTokenIdAndType(this.toLong(), objectTypeId)
}

fun MutableList<ObjectTokenIdAndType>.addOfType(objectTypeId: ObjectTypeId, vararg objectTokenId: ObjectTokenId) {
    addAll(ofType(objectTypeId, *objectTokenId))
}

fun ofType(objectTypeId: ObjectTypeId, vararg objectTokenId: ObjectTokenId): List<ObjectTokenIdAndType> {
    val list = mutableListOf<ObjectTokenIdAndType>()
    for (i in objectTokenId) {
        list.add(
            i.withType(objectTypeId.objTypeId())
        )
    }
    return list
}

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

fun Long.withType(objectTypeId: ObjectTypeId): ObjectTokenIdAndType {
    return ObjectTokenIdAndType(this, objectTypeId)
}

fun SimulationComponent.withGenerateTokens(ids: MutableList<ObjectTokenIdAndType>.() -> Unit): SimulationComponent {
    val objectTokenSet = objectTokenSet()
    val generator = objectTokenGenerator()
    val mutableList = mutableListOf<ObjectTokenIdAndType>()
    mutableList.ids()
    val ocNet = ocNet()

    for (i in mutableList) {
        objectTokenSet.add(
            generator.generate(
                type = ocNet.objectTypeRegistry[i.objectTypeId],
                id = i.objectTokenId
            )
        )
    }
    return this
}

fun SimulationComponent.addTokens(block: AddTokensBlock.() -> Unit): SimulationComponent {
    val generator = newTokenGenerationFacade()
    val objectTokenRealAmountRegistry = objectTokenRealAmountRegistry()
//    val state = state()
//    val ocNet = ocNet()
    val receiver = AddTokensBlockImpl(this)
    receiver.block()
//    val deltaMarking = PlaceToObjectMarking()
    for ((place, amount) in receiver.placeToAmount) {
        objectTokenRealAmountRegistry.incrementRealAmountAt(place, amount)

//        for (i in 0 until amount) {
//            val objectTypeId = ocNet.placeToObjectTypeRegistry[place]
//            val objectToken = generator.generateRealToken(objectTypeId)
//            deltaMarking.add(place, objectToken.id)
//        }
    }
//    state.pMarking.plus(deltaMarking.toImmutable())
    return this
}

interface AddTokensBlock {
    fun forPlace(place : PetriAtomId, amount: Int)
}

private class AddTokensBlockImpl(private val simulationComponent: SimulationComponent) : AddTokensBlock {
    val placeToAmount: MutableMap<PetriAtomId, Int> = mutableMapOf()

    override fun forPlace(place: PetriAtomId, amount: Int) {
        val amount = placeToAmount.getOrPut(place) { 0 } + amount
        placeToAmount.put(place, amount)
    }
}

data class FacadeSim(
    val component: SimulationComponent,
    val task: SimulationTask,
    val config: SimulationConfig,
)

//fun facadeCreateSimulation(
//    simulationConfig: SimulationConfig,
//    developmentDebugConfig: DevelopmentDebugConfig = fastNoDevSetup()
//
//): FacadeSimulation {
//    return
//}


fun SimulationComponent.transition(t: PetriAtomId): Transition {
    val ocNet = ocNet()
    return ocNet.petriAtomRegistry.getTransition(t)
}

fun SimulationComponent.mockTransitionBufferInfo(
    t: PetriAtomId,
    block: MutableCollection<BatchKeyWithBuffer>.() -> Unit,
): TokenGroupedInfo {

    val trans = transition(t)
    val simBatchGroupingStrategy = batchGroupingStrategy()

    return mockk<TokenGroupedInfo> {
        every { transition } returns trans
        every { tokenGroupingStrategy } returns simBatchGroupingStrategy

        val mutableCollection = mutableListOf<BatchKeyWithBuffer>()
        mutableCollection.block()

        for (i in mutableCollection) {
            every {
                getTokenSetBy(
                    i.batchKey.objectTypeId,
                    i.batchKey.arcMeta,
                )
            } returns i.tokenSet
        }
    }
}


data class BatchKey(val objectTypeId: ObjectTypeId, val arcMeta: ArcMeta)
data class BatchKeyWithBuffer(val batchKey: BatchKey, val tokenSet: TokenSet)

fun ObjectTypeId.withArcMeta(arcMeta: ArcMeta): BatchKey {
    return BatchKey(this.objTypeId(), arcMeta)
}

fun String.objTypeId(): ObjectTypeId {
    return if (USE_SPECIAL_SYMBOL_OBJ_TYPE_NAME) {
        prependId()
    } else {
        this
    }
}

fun BatchKey.withTokenBuffer(set: TokenSet): BatchKeyWithBuffer {
    return BatchKeyWithBuffer(this, set)
}


infix fun MutableCollection<BatchKeyWithBuffer>.unaryPlus(batchKey: BatchKeyWithBuffer) {
    add(batchKey)
}
